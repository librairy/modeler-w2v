# Modeler W2V
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/5e3e2fe9b1c242b6a4a13b7e6459b68e)](https://www.codacy.com/app/cbadenes/modeler-w2v)
[![Build Status](https://travis-ci.org/librairy/modeler-w2v.svg?branch=master)](https://travis-ci.org/librairy/modeler-w2v)

Build a Word Embedding Model based on Word2Vec for a given domain

## Get Started!

A prerequisite to consider is to have installed [Docker-Compose](https://docs.docker.com/compose/) in your system.

You can run this service in a isolated way (see *Distibuted Deployment* section) or as extension of the [api](https://github.com/librairy/api).
In that case, add the following services to the existing `docker-compose.yml` file:

```yml
modelerLDA:
  container_name: modelerW2V
  image: librairy/modeler-w2v
  links:
      - column-db
      - document-db
      - graph-db
      - event-bus
```

and then, deploy it by typing:

```sh
$ docker-compose up
```
That's all!! **modeler-w2v** should be run in your system now along with **librairy**.

## Distributed Deployment

Instead of deploy all containers as a whole, you can deploy each of them independently. It is useful to run the service in a distributed way deployed in several host-machines.

- **modelerLDA**:
    ```sh
    $ docker run -it --rm --name modelerW2V librairy/modeler-w2v
    ```

Remember that by using the flags: `-it --rm`, the services runs in foreground mode. Instead, you can deploy it in background mode as a domain service by using: `-d --restart=always`
