## Release Checklist

- Bump the versions in:
    - `project.clj`.
    - `resources/FXL_REPL_RELEASED_VERSION`.
- Ensure that the pre-release CI steps pass with `make ci`.
- Deploy the main library with `lein deploy clojars`.
- Push the newly built container to DockerHub with `make docker-release`.
- Merge the library version-bump branch.
