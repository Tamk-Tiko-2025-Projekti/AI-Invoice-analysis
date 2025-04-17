@echo off
pushd frontend
echo "Installing frontend dependencies..."
call npm install
echo "Building frontend..."
call npm run build
popd
xcopy frontend\dist\* spring_back\App\src\main\resources\static /E /I /Y
pushd spring_back\App
IF NOT EXIST "venv" (
    echo "Creating python virtual environment..."
    python -m venv venv
)
call venv\Scripts\activate
echo "Installing python dependencies..."
call pip install -r requirements.txt
call deactivate
popd
