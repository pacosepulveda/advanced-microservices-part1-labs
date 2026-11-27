package com.advancedorders.orders.state;

import java.sql.*;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/orders/training/state")
@ConditionalOnProperty(name="training.state.enabled", havingValue="true")
public class OrderStateController {
    private final String db, user, password;
    private final RestClient inventory, payments;
    public OrderStateController(
        @Value("${training.state.db}") String db,
        @Value("${training.state.user}") String user,
        @Value("${training.state.password}") String password,
        @Value("${training.state.inventory-url}") String inventoryUrl,
        @Value("${training.state.payments-url}") String paymentsUrl) {
        this.db=db; this.user=user; this.password=password;
        this.inventory=client(inventoryUrl); this.payments=client(paymentsUrl);
    }
    private RestClient client(String url) {
        var factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2)).build());
        factory.setReadTimeout(Duration.ofSeconds(3));
        return RestClient.builder().baseUrl(url).requestFactory(factory).build();
    }
    private Connection connect() throws SQLException { return DriverManager.getConnection(db,user,password); }
    public record Command(String orderId,String sku,int quantity,boolean decline) {}
    public record Event(int version,String kind,String sku,int quantity,boolean decline) {}
    public record State(String orderId,String sku,int quantity,boolean decline,String status,int version) {}
    public record Decision(String status) {}
    public record Reservation(String orderId,String sku,int quantity) {}
    public record Payment(String orderId,boolean decline) {}
    static State fold(String id,List<Event> events) {
        if(events.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Unknown order");
        Event first=events.get(0);
        String status="NONE";
        int expected=1;
        for(Event e:events){
            if(e.version()!=expected++ || !Objects.equals(e.sku(),first.sku()) || e.quantity()!=first.quantity() || e.decline()!=first.decline())
                throw new IllegalStateException("Invalid event history");
            boolean allowed=switch(status){
                case "NONE" -> e.kind().equals("CREATED");
                case "NEW" -> Set.of("RESERVED","RESERVATION_REJECTED").contains(e.kind());
                case "RESERVED" -> Set.of("PAYMENT_APPROVED","PAYMENT_DECLINED").contains(e.kind());
                case "PAID" -> e.kind().equals("CONFIRMED");
                case "PAYMENT_FAILED" -> e.kind().equals("RELEASED");
                default -> false;
            };
            if(!allowed) throw new IllegalStateException("Invalid transition "+status+" / "+e.kind());
            status=switch(e.kind()){
                case "CREATED" -> "NEW";
                case "RESERVED" -> "RESERVED";
                case "RESERVATION_REJECTED","RELEASED" -> "CANCELLED";
                case "PAYMENT_APPROVED" -> "PAID";
                case "PAYMENT_DECLINED" -> "PAYMENT_FAILED";
                case "CONFIRMED" -> "CONFIRMED";
                default -> throw new IllegalStateException("Unknown event");
            };
        }
        return new State(id,first.sku(),first.quantity(),first.decline(),status,expected-1);
    }
    private List<Event> history(Connection c,String id) throws SQLException {
        var events=new ArrayList<Event>();
        try(var p=c.prepareStatement("SELECT version,kind,sku,quantity,decline FROM orders.event WHERE order_id=? ORDER BY version")){
            p.setString(1,id);
            try(var rs=p.executeQuery()){while(rs.next()) events.add(new Event(rs.getInt(1),rs.getString(2),rs.getString(3),rs.getInt(4),rs.getBoolean(5)));}
        }
        return events;
    }
    private void append(Connection c,State s,String kind) throws SQLException {
        try(var p=c.prepareStatement("INSERT INTO orders.event(order_id,version,kind,sku,quantity,decline) VALUES (?,?,?,?,?,?)")){
            p.setString(1,s.orderId());p.setInt(2,s.version()+1);p.setString(3,kind);
            p.setString(4,s.sku());p.setInt(5,s.quantity());p.setBoolean(6,s.decline());p.executeUpdate();
        }
    }
    @PostMapping("/commands")
    public synchronized State create(@RequestBody Command cmd) throws SQLException {
        if(cmd.orderId()==null || !cmd.orderId().matches("[A-Za-z0-9-]{1,64}") || cmd.sku()==null || cmd.sku().isBlank() || cmd.quantity()<1)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid command");
        try(var c=connect()){
            c.setAutoCommit(false);
            try {
                try(var p=c.prepareStatement("INSERT INTO orders.stream(id) VALUES (?) ON CONFLICT DO NOTHING")){
                    p.setString(1,cmd.orderId());p.executeUpdate();
                }
                try(var p=c.prepareStatement("SELECT id FROM orders.stream WHERE id=? FOR UPDATE")){
                    p.setString(1,cmd.orderId());try(var rs=p.executeQuery()){rs.next();}
                }
                var events=history(c,cmd.orderId());
                if(events.isEmpty()){
                    append(c,new State(cmd.orderId(),cmd.sku(),cmd.quantity(),cmd.decline(),"NEW",0),"CREATED");
                } else {
                    State s=fold(cmd.orderId(),events);
                    if(!s.sku().equals(cmd.sku())||s.quantity()!=cmd.quantity()||s.decline()!=cmd.decline())
                        throw new ResponseStatusException(HttpStatus.CONFLICT,"Order ID reused with different command");
                }
                State s=fold(cmd.orderId(),history(c,cmd.orderId()));c.commit();return s;
            } catch(Exception e){c.rollback();throw e;}
        }
    }
    @GetMapping("/{id}/events")
    public List<Event> events(@PathVariable String id) throws SQLException {try(var c=connect()){return history(c,id);}}
    @GetMapping("/{id}/aggregate")
    public State aggregate(@PathVariable String id) throws SQLException {try(var c=connect()){return fold(id,history(c,id));}}
    // Transport helpers provided: students design and implement the state machine.
    private String reserve(State s) {
        Decision d=inventory.post().uri("/api/inventory/training/state/reserve")
            .body(new Reservation(s.orderId(),s.sku(),s.quantity())).retrieve().body(Decision.class);
        return Objects.requireNonNull(d).status();
    }
    private String decidePayment(State s) {
        Decision d=payments.post().uri("/api/payments/training/state/decide")
            .body(new Payment(s.orderId(),s.decline())).retrieve().body(Decision.class);
        return Objects.requireNonNull(d).status();
    }
    private String release(State s) {
        Decision d=inventory.post().uri("/api/inventory/training/state/release")
            .body(new Reservation(s.orderId(),s.sku(),s.quantity())).retrieve().body(Decision.class);
        return Objects.requireNonNull(d).status();
    }
    private State recordTransition(State s,String kind) throws SQLException {
        try(var c=connect()){
            c.setAutoCommit(false);
            try {append(c,s,kind);State next=fold(s.orderId(),history(c,s.orderId()));c.commit();return next;}
            catch(Exception e){c.rollback();throw e;}
        }
    }
    // Training: one orchestrator process, one step per call; no DB transaction spans remote HTTP.
    @PostMapping("/{id}/advance")
    public synchronized State advance(@PathVariable String id) throws SQLException {
        State s=aggregate(id);
        String kind=switch(s.status()){
            case "NEW" -> switch(reserve(s)){
                case "RESERVED" -> "RESERVED";
                case "REJECTED" -> "RESERVATION_REJECTED";
                default -> throw new IllegalStateException("Unexpected reservation status");
            };
            case "RESERVED" -> switch(decidePayment(s)){
                case "APPROVED" -> "PAYMENT_APPROVED";
                case "DECLINED" -> "PAYMENT_DECLINED";
                default -> throw new IllegalStateException("Unexpected payment status");
            };
            case "PAID" -> "CONFIRMED";
            case "PAYMENT_FAILED" -> {
                if(!"RELEASED".equals(release(s))) throw new IllegalStateException("Release not confirmed");
                yield "RELEASED";
            }
            default -> null;
        };
        return kind==null?s:recordTransition(s,kind);
    }
    @GetMapping("/{id}/query")
    public Map<String,Object> query(@PathVariable String id) throws SQLException {
        try(var c=connect();var p=c.prepareStatement("SELECT sku,quantity,status,version FROM orders.read_model WHERE order_id=?")){
            p.setString(1,id);
            try(var rs=p.executeQuery()){
                if(!rs.next()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Projection not available");
                return Map.of("orderId",id,"sku",rs.getString(1),"quantity",rs.getInt(2),"status",rs.getString(3),"version",rs.getInt(4));
            }
        }
    }
    @PostMapping("/{id}/project")
    public synchronized Map<String,Object> project(@PathVariable String id) throws SQLException {
        State s=aggregate(id);
        try(var c=connect();var q=c.prepareStatement("INSERT INTO orders.read_model(order_id,sku,quantity,status,version) VALUES(?,?,?,?,?) ON CONFLICT(order_id) DO UPDATE SET sku=EXCLUDED.sku,quantity=EXCLUDED.quantity,status=EXCLUDED.status,version=EXCLUDED.version WHERE orders.read_model.version < EXCLUDED.version")){
            q.setString(1,id);q.setString(2,s.sku());q.setInt(3,s.quantity());q.setString(4,s.status());q.setInt(5,s.version());q.executeUpdate();
        }
        return query(id);
    }
    @DeleteMapping("/{id}/projection")
    public synchronized void clearProjection(@PathVariable String id) throws SQLException {
        try(var c=connect();var p=c.prepareStatement("DELETE FROM orders.read_model WHERE order_id=?")){p.setString(1,id);p.executeUpdate();}
    }
}
