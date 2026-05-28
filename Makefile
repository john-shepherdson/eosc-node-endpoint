IMAGE_NAME=$(shell ./mvnw -pl eosc-node-endpoint-service help:evaluate -Dexpression=spring-boot.build-image.imageName -q -DforceStdout)
TARGET=$(shell find eosc-node-endpoint-service/target -maxdepth 1 -name "eosc-node-endpoint-service-*.jar" ! -name "*.original" 2>/dev/null)
HOST_UID=$(shell id -u)
HOST_GID=$(shell id -g)
CONFIG ?= file:compose/config/application.properties

.PHONY: build run docker-build docker-push docker-compose docker-compose-down

build:
	./mvnw clean package

run:
	@trap 'exit 0' INT; SPRING_CONFIG_ADDITIONAL_LOCATION=$(CONFIG) java -jar $(TARGET) \
		--capabilities.filepath=compose/config/capabilities.json

docker-build:
	./mvnw clean package -pl eosc-node-endpoint-service -am spring-boot:build-image-no-fork

docker-push:
	docker image push $(IMAGE_NAME)

docker-compose:
	IMAGE_NAME=$(IMAGE_NAME) HOST_UID=$(HOST_UID) HOST_GID=$(HOST_GID) docker compose -f compose/compose.yaml up

docker-compose-down:
	IMAGE_NAME=$(IMAGE_NAME) HOST_UID=$(HOST_UID) HOST_GID=$(HOST_GID) docker compose -f compose/compose.yaml down

default: docker-build docker-push

.DEFAULT_GOAL := default
