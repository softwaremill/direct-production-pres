# Production-ready direct-style Scala

Code for the talk from Scalar 2025. The video is available here: https://www.youtube.com/watch?v=Vy-0LL0REnI

## How to run locally

1. Start the Kafka broker & associated services using `kafka/docker-compose.yml`. After some time, the control center
   should be available at `http://localhost:9021`.
2. Start the Posgres database & Grafana LGTM stack (monitoring, logging, tracing) using `docker-compose.yml`. Once
   again, after some time, the Grafana dashboard should be available at `http://localhost:3000`. You can login with
   `admin`/`admin`.
3. Start the backend using `./backend-start.sh`. This should create the tables and start the API. OpenAPI documentation
   should be available at `http://localhost:8080/api/v1/docs/`.
4. The travel page which is shown in the talk uses two external services (which are part of the distributed trace).
   From an IDE, start the `scalar.directdemo.services.PickRandomService` and `scalar.directdemo.services.TravelModeService`
   apps.
5. You can now try calling the trip service via OpenAPI. Once you do that, you can check in the Grafana dashboard if
   the trace shows up - after a couple of seconds. Go to "explore" from the "hamburger" menu, then switch from
   Prometheus to Tempo, switch to "Search" and take a look at the available traces.
6. Finally, run `./frontend-start.sh`. This should open your default browser with `http://localhost:8081/`. Go to
   `http://localhost:8081/trip` and click the button - the backend service should be called.
7. You might also open Kafka's control center, choose the `trips` topic, then the "Messages" tab, to view the result
   of the background streaming.
