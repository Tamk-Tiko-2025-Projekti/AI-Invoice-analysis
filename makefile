.PHONY: all setup-venv start-back start-front start stop help

all: setup-venv start

setup-venv:
	@echo "Setting up virtual environment..."
	@if [ ! -d "spring_back/App/venv" ]; then \
	python3 -m venv spring_back/App/venv; \
	echo "starting venv and installing dependencies"; \
	. spring_back/App/venv/bin/activate && pip install -r ./spring_back/App/requirements.txt; \
	else \
	echo "Virtual environment already exists"; \
	fi


start: start-back start-front

start-back:
	@echo "Starting spring boot server..."
	cd spring_back/App && ./gradlew bootRun
	@echo "Spring Boot server started."

start-front:
	@echo "Starting React front-end..."
	cd frontend && npm run dev
	@echo "React front-end started."

stop:
	@echo "Stopping servers..."
	@pkill -f 'java -jar' || true
	@pkill -f 'npm run dev' || true
	@echo "Servers stopped."

help:
	@echo "Makefile commands:"
	@echo "  all          - Setup virtual environment and start servers (default target)"
	@echo "  setup-venv   - Setup virtual environment"
	@echo "  start        - Start both Spring Boot and React servers"
	@echo "  start-back   - Start Spring Boot server"
	@echo "  start-front  - Start React front-end"
	@echo "  stop         - Stop all servers"
	@echo "  help         - Show this help message"
