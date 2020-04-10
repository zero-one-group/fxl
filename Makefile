DOCKERNAME=zeroonetechnology/fxl

build:
	cp project.clj docker/project.clj
	docker build -f docker/Dockerfile -t $(DOCKERNAME) docker

dock: build
	docker run --rm -v $(PWD):/root/fxl \
		-w /root/fxl \
		-it $(DOCKERNAME) \
		/bin/bash

continuous-integration: build
	docker run --rm -v $(PWD):/root/fxl \
		-w /root/fxl \
		-it $(DOCKERNAME) \
		/bin/bash -c \
		"echo 'Starting CI pipeline...' \
		 && echo 'Running Tests...' && lein midje  \
		 && echo 'Running Kondo...' && clj-kondo --lint src test \
		 && echo 'Running Kibit...' && lein kibit src test \
		 && echo 'Running Cloverage...' && lein cloverage --fail-threshold 90 --ns-exclude-regex zero-one.fxl.specs"

all-tests: unit-tests coverage

unit-tests:
	lein midje

coverage:
	lein cloverage --fail-threshold 90 --ns-exclude-regex zero-one.fxl.specs

lint:
	clj-kondo --lint src test
	lein kibit src test
