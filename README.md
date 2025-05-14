# AI invoice analysis system
This application is an invoice analysis system designed to extract structured data from invoices using a large language model such as GPT-4o. The application can handle both PDF and image invoices.

## System Requirements
- Java 21 or higher
- Kotlin 2.1.0 or higher
- Gradle 8.12 or higher
- Python 3.10.0 or higher

## How to run
### Docker:
To build and run the program in a docker container, first navigate to the repository root in a terminal. Then, run `docker build -t <insert name here> .`. After the docker container has been built, run it with `Docker run -p 8080:8080 <insert name here>`. After the Docker container is running, navigate to http://localhost:8080/ in a web browser.

### Windows:
Navigate to repository root, then run `setup.bat` to install dependencies and setup the python virtual environment and then run `start.bat` to start the server. Then navigate to http://localhost:8080/ in a web browser.

### Linux:
Navigate to repository root in a terminal, then run the `make` command to install dependencies, set up the python virtual environment and start the server. Then navigate to http://localhost:8080/ in a web browser. Run `make help` to see information about other targets in the makefile.

### Note
The program needs an API key to make calls to the OpenAI API. If there is no .env file in  `/spring_back/App/`, create it. Then, add the API key to the .env file under the `OPENAI_API_KEY` variable e.g. `OPENAI_API_KEY="<insert API key here>"`.

## Authors
- Nitai Spira 
- Mika Kuusisto
- Tuuli Marttila
- Santeri Sillanaukee

## License
This project is licensed under the MIT license.
