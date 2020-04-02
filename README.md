# fxl

TODOs:
- (X) Read plain Excel file.
- (X) Write plain Excel file.
- (X) Add styles:
    - (X) Font name, size, colour.
    - (X) Font: bold, italic, underline, strikethrough
    - (X) Alignment.
    - (X) Border.
- (X) Read styled Excel file.
- Write styled Excel file.
- Error handling with failjure.
- Add data formats.
- Utility functions: easy transition to docjure and excel-clj.
- CI pipeline: midje, joker, clj-kondo and cloverage.
- README: what is fxl, example, installation and license.
- Ship as a library.
- Nice to haves:
    - Support to Google Sheet API.
    - Support merged cells.
    - Support data-val cells.
    - Property-based testing.

The project uses [Midje](https://github.com/marick/Midje/).

## How to run the tests

`lein midje` will run all tests.

`lein midje namespace.*` will run only tests beginning with "namespace.".

`lein midje :autotest` will run all the tests indefinitely. It sets up a
watcher on the code files. If they change, only the relevant tests will be
run again.
