# World Of Pay

### Running in terminal (clone & run)
Assuming installed tools git, sbt, java etc:
```
git clone https://github.com/hczerpak/worldofpay.git
cd worldofpay
sbt run
```

### Description of endpoints

##### 1. To create an offer send:
```
curl -d "{\"description\": \"Strawberry Protein Shake\", \"currency\": \"IDR\", \"price\": 2000000, \"expires\": \"10/10/2020\"}" -H "content-type:application/json" http://localhost:9000/offers
```
expect HTTP code 201 and an offer id back if created successfully. It will return HTTP 400 if expiry date has passed.

##### 2. To query an offer send:

```
GET /offer/id
```
with `id` received from POST above. Expect a JSON back similar to one from POST but with `id` added to the properties and HTTP OK, or 404 if no offer found

##### 3. To close an offer send:
```
DELETE /offer/id
```
with `id` received from POST above. Expect an empty body and HTTP OK, or 404 if no offer found

My assumptions:
- offer expiration defied as LocalDate, without time for simplicity.
- minimal validation on the input. Only expiration date checked for being in the future.