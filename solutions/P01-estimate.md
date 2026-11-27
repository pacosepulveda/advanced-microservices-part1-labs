# P01 — Solución de referencia: estimación
Consulta el código resuelto en esta rama después de intentar el ejercicio.
## Cliente
```java
public ShippingQuote quote(String destination, int weightGrams) {
    try {
        ShippingQuote result = client.get().uri(u -> u.path("/api/shipping/quote")
            .queryParam("destination",destination).queryParam("weightGrams",weightGrams).build())
            .retrieve().body(ShippingQuote.class);
        if (result == null) throw new ShippingUnavailableException("Empty shipping response",null);
        return result;
    } catch (org.springframework.web.client.RestClientException e) {
        throw new ShippingUnavailableException("Shipping call failed",e);
    }
}
```
## Servicio
```java
public OrderEstimateResponse estimate(String sku,int quantity,String destination,int weightGrams) {
    if(sku==null||sku.isBlank()||quantity<1||destination==null||!destination.matches("[A-Z]{2}")||weightGrams<1||weightGrams>10000)
        throw new IllegalArgumentException("Invalid estimate request");
    var stock=inventory.getInventory(sku);
    boolean enough=stock.available()>=quantity;
    var quote=enough?shipping.quote(destination,weightGrams):null;
    return new OrderEstimateResponse(stock.sku(),quantity,enough,stock.available(),quote);
}
```
## Controller
```java
@GetMapping("/estimate")
public com.advancedorders.orders.api.OrderEstimateResponse estimate(
    @RequestParam("sku") String sku,@RequestParam("quantity") int quantity,
    @RequestParam("destination") String destination,@RequestParam("weightGrams") int weightGrams) {
    return service.estimate(sku,quantity,destination,weightGrams);
}
```
Gateway: Path=/stock/{sku}, SetPath=/api/inventory/{sku}, URI inventory URL existing config. Añadir sin quitar rutas. Probar make p01-test y make p01-up; make p01-verify. Prueba adicional: mock inventory throws InventoryUnavailableException; assertThatThrownBy, verifyNoInteractions(shipping). Esperados: precio 499 para ES/500, stock 42, sin stock shipping null, inválido 400, Shipping caído 502. No afirmar validación hasta ejecutar solución.
