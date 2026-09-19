up:
	docker compose -f infra/compose/docker-compose.yaml up -d
down:
	docker compose -f infra/compose/docker-compose.yaml down
test-order:
	cd services/order && ./gradlew -q test
