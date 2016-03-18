# Librairy Modeler W2V - Files [![Build Status](https://travis-ci.org/librairy/modeler-w2v.svg?branch=develop)](https://travis-ci.org/librairy/modeler-w2v)

Build a Word Embedding Model by W2V

## Get Start!

The only prerequisite to consider is to install [Docker](https://www.docker.com/) in your system.

Then, run `docker-compose up` by using this file `docker-compose.yml`:  

```yml
modelerW2V:
  container_name: modelerW2V
  image: librairy/modeler-w2v
  restart: always
  links:
      - column-db
      - document-db
      - graph-db
      - event-bus
```

That's all!! **librairy comparator cos** should be run in your system now!

Instead of deploy all containers as a whole, you can deploy each of them independently. It is useful to run the service distributed in several host-machines.

## FTP Server

```sh
docker run -it --rm --name modelerW2V librairy/modeler-w2v
```

Remember that, by using the flags: `-it --rm`, the services runs in foreground mode, if you want to deploy it in background mode,  or even as a domain service, you should use: `-d --restart=always`
