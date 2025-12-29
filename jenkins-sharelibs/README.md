docker compose for jenkins

```yaml
version: "3"

services:
  docker:
    image: docker:dind
    privileged: true
    environment:
      - "DOCKER_TLS_CERTDIR=/certs"
    volumes:
      - ./data/certs:/certs/client
      - ./data/jenkins:/var/jenkins_home
    ports:
      - 2376:2376
  jenkins:
    image: jenkins-bo:1.0
    restart: always
    ports:
      - 8080:8080
    environment:
      - 'DOCKER_HOST=tcp://docker:2376'
      - 'DOCKER_CERT_PATH=/certs/client'
      - 'DOCKER_TLS_VERIFY=1'
    volumes:
      - ./data/certs:/certs/client
      - ./data/jenkins:/var/jenkins_home
```