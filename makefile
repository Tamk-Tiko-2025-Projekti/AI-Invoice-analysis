.PHONY: all setup-venv build start-back start-front start-both stop help

all: setup-venv build-front start-back

setup-venv:
	@if [ ! -d "spring_back/App/venv" ]; then \
		echo "Creating virtual environment..."; \
		python3 -m venv spring_back/App/venv; \
	fi
	@echo "starting venv and installing dependencies";
	@. spring_back/App/venv/bin/activate && pip install -r ./spring_back/App/requirements.txt; \

start-both: start-back start-front

start-back:
	@echo "Starting spring boot server..."
	cd spring_back/App && ./gradlew bootRun
	@echo "Spring Boot server started."

start-front:
	@echo "Starting React front-end..."
	cd frontend && npm run dev
	@echo "React front-end started."

build-front:
	@echo "Building React front-end..."
	cd frontend && npm run build
	@echo "React front-end built."
	@echo "Current working directory: $(shell pwd)"
	@if [ ! -d "spring_back/App/main/resources/static/" ]; then \
		echo "Creating static directory..."; \
		mkdir -p spring_back/App/src/main/resources/static/; \
	fi
	@echo "Copying build to Spring Boot resources..."
	@cp -r frontend/dist/* spring_back/App/src/main/resources/static/

stop:
	@echo "Stopping servers..."
	@pkill -f 'java -jar' || true
	@pkill -f 'npm run dev' || true
	@echo "Servers stopped."

help:
	@echo "Makefile commands:"
	@echo "  all          - Setup virtual environment and start servers (default target)"
	@echo "  setup-venv   - Setup virtual environment"
	@echo "  start-both   - Start both Spring Boot and React servers"
	@echo "  start-back   - Start Spring Boot server"
	@echo "  start-front  - Start React front-end"
	@echo "  build-front  - Build React front-end and copy to spring_back/App/src/main/resources/static/"
	@echo "  stop         - Stop all servers"
	@echo "  help         - Show this help message"
