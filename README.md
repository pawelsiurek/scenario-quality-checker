# Scenario Quality Checker

The SQC application is a specialized tool designed for analysts to document, qualify and detect errors in functional requirements written in scenario format. By providing automated feedback on scenarios, it ensures higher quality documentation before development begins.

## Features:
- Quantitative analysis: get metrics and data on the documented scenarios.
- Error detection: automatically identify problems or inconsistencies in functional requirements.
- Dual access: use the application via a user-friendly GUI or integrate it into your workflow via a remote API.

## Scenario Format Guidelines:
To ensure that the SQC application can process your requirements, follow these formatting standards:
1. **Header**: Every scenario must include a Title, External Actors, and System Actors.
2. **Steps**: Scenarios consist of sequential text-based steps.
3. **Nesting**: Steps can contain sub-scenarios at any level of depth.
4. **Logic Keywords**: Use the following keywords to define flow control: **IF/ELSE, FOR EACH**

## Example
**Title**: Book addition \
**Actors**: Librarian \
**System actor**: System

* Librarian selects options to add a new book item
* A form is displayed.
* Librarian provides the details of the book.
* IF: Librarian wishes to add copies of the book
    * Librarian chooses to define instances
    * System presents defined instances
    * FOR EACH: instance:
        * Librarian chooses to add an instance
        * System prompts to enter the instance details
* Librarian confirms book addition.
* System informs about the correct addition of the book.

## Integration
The application provides a Remote API, allowing it to be integrated with existing project management and documentation tools.