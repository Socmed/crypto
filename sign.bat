if exist jre ( 
    set javaDir=jre\bin\
)

%javaDir%java.exe -Xmx1024m -cp "classes;lib/*;conf" -Degl.runtime.mode=desktop egl.tools.SignTransactionJSON
