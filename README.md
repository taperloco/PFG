# Recado: aplicación móvil distribuida de mensajería geolocalizada 
UNIVERSIDAD NACIONAL DE EDUCACIÓN A DISTANCIA (UNED)<br/>
ESCUELA TÉCNICA SUPERIOR DE INGENIERÍA INFORMÁTICA<br/>
Proyecto de Fin de Grado <br/>
# Introducción  
Las aplicaciones de mensajería tienen una importancia considerable en nuestras comunicaciones diarias, esto es así porque nos permiten comunicarnos instantáneamente 
sin importar distancias. Este Proyecto de Fin de Grado se centra en un conjunto de posibilidades que puede ofrecer el uso de una aplicación móvil de mensajería en pequeñas distancias, gracias al uso de la geolocalización. Principalmente permite:<br/>
• Escribir y leer mensajes (Recados) sin destinatario específico, que permanecen asociados a una ubicación geográfica concreta. <br/>
• Enviar mensajes a usuarios próximos. <br/>
• Visualizar usuarios y mensajes en un mapa con información del entorno. <br/>
• Uso extensivo del mapa como interfaz de usuario. <br/>
Se trata de un sistema distribuido, es decir, los componentes localizados en computadores, conectados en red, comunican y coordinan sus acciones únicamente mediante el paso de mensajes. Además sigue el modelo cliente-servidor.
# Cliente
Se desarrolla en Android, utilizando el lenguaje Kotlin y la librería Jetpack Compose. Se usa este lenguaje pues es recomendado por Google desde 2019 para programar en Android. Se trata de un programa expresivo y conciso que reduce errores comunes y se integra fácilmente con aplicaciones existentes (escritas por ejemplo en Java). <br/>
La ejecución del cliente se puede realizar cargando el código en el IDE de Android Studio y ejecutándolo en uno de los simuladores que ofrece (o, si se prefiere, en un dispositivo real). Alternativamente se puede instalar el archivo APK.
# Servidor
Se desarrolla en Python, uno de los lenguajes más usados en back end. La API usa FLASK RestFul, que es una extensión de Flask que añade soporte para construir APIs 
REST de forma rápida. Además de MongoDB como base de datos NoSQL.<br/> 
La ejecución del servidor (para desarrollo) se puede realizar cargando el código en el IDE de Microsoft Visual Studio Code y ejecutando el archivo main. 
# Nota
Para más información me remito a la Memoria del PFG, donde se detalla pormenorizadamente el proceso de instalación, tanto para desarrollo (debug) como para despliegue (deployment). 
