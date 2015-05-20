@setlocal

@rem to echo or not to echo
@echo off
@if not "%ECHO%" == "" echo %ECHO% 

@rem check if email has been provided
if "%1" == "" goto noemail

@rem prepare gh-pages clone directory
set "CLONE_DIR=%~dp0\tmp"
set "CLONE_DIR=%CLONE_DIR:\\=\%"
if exist "%CLONE_DIR%" rd /q /s "%CLONE_DIR%" >nul
md %CLONE_DIR% >nul

@rem clone gh-pages
pushd %~dp0
call git branch -q -D gh-pages
call git fetch -q origin gh-pages:gh-pages
call git clone -q -l -s -b gh-pages ../.. "%CLONE_DIR%\gh-pages"
popd
cd "%CLONE_DIR%\gh-pages"

@rem cleanup old update-site
call git rm -q -r *

@rem prepare new update-site
robocopy /E /NP "%~dp0\target\repository" .\ >nul
robocopy /E /NP "%~dp0\root-files" .\ >nul

@rem commit
call git add .
call git config --local --add user.email %1
call git commit -q -m "new release published"
call git push -q
echo git push origin gh-pages:gh-pages
goto end

:noemail
echo %0 ^<email-address^>

:end
@endlocal
