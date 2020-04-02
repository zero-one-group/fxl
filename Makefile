DOCKERNAME=zeroonetechnology/fxl

build:
	cp project.clj docker/project.clj
	docker build -f docker/Dockerfile -t $(DOCKERNAME):ci docker

dock: build
	docker run --rm -v $(PWD):/root/fxl \
		-w /root/fxl \
		-it $(DOCKERNAME):ci \
		/bin/bash

continuous-integration: build
	docker run --rm -v $(PWD):/root/fxl \
		-w /root/fxl \
		-it $(DOCKERNAME):ci \
		/bin/bash -c \
		"echo 'Starting CI pipeline...' \
		 && echo 'Running Tests...' && lein midje  \
		 && echo 'Running Joker...' && joker --lint --working-dir . \
		 && echo 'Running Kondo...' && clj-kondo --lint src test \
		 && echo 'Running Cloverage...' && lein cloverage --fail-threshold 90"
