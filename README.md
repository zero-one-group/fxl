# fxl

TODOs:
- (X) Read plain Excel file.
- Write plain Excel file.
- Read styled Excel file.
- Write styled Excel file.
- Add more styles:
    - Font name, size, colour.
    - Font: bold, italic, underline, strikethrough
    - Num format.
    - Alignment.
    - Border.
- Support merged cells.
- Support cond-style cells.
- Support data-val cells.

The project uses [Midje](https://github.com/marick/Midje/).

## How to run the tests

`lein midje` will run all tests.

`lein midje namespace.*` will run only tests beginning with "namespace.".

`lein midje :autotest` will run all the tests indefinitely. It sets up a
watcher on the code files. If they change, only the relevant tests will be
run again.
