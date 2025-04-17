pushd frontend
call npm install
call npm run build
popd
xcopy frontend\dist\* spring_back\App\src\main\resources\static /E /I /Y
pushd spring_back\App
call venv\Scripts\activate
call pip install -r requirements.txt
call deactivate
popd
