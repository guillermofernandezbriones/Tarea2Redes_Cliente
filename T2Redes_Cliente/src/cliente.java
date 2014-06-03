import java.io.*;
import java.net.*;
import java.util.*;

public class cliente
{
	int puerto = 6666;
	boolean repetidor = true;
	
	// Inicio de ejecucion
	public static void main(String [] array)	
	{
		cliente instanciaServ = new cliente();	
		instanciaServ.correr();
	}
	
	boolean correr()
	{
		System.out.println("MSJ: -Servidor corriendo-");
		
		try
		{
			ServerSocket servSock = new ServerSocket(6666);
			System.out.println("MSJ: -Esperando conexion...-");
			
			// Loop infinito que queda en espera de conexiones que entren, a las que se les asocia un thread
			while(repetidor)
			{
				Socket entrante = servSock.accept();
				request rqCliente = new request(entrante);
				rqCliente.start();
				//agregar if con el botón de parado del repetidor
			}
			
		}
		catch(Exception e)
		{
			System.out.println("MSJ: -Error en el servidor: " + e.toString());
		}
		
		return true;
	}	
}

class request extends Thread
{
	int contador = 0;

	private Socket scliente = null;	// Request del cliente de turno
   	private PrintWriter out = null;	// Buffer donde se escribe respuesta a las peticiones

   	request(Socket ps)
   	{
		System.out.println(currentThread().toString() + " - " + "El contador es: " + contador);
		contador++;
		
		// El socket de cliente enviado como request se asocia a scliente
		scliente = ps;
		
		// Se setea una baja prioridad
		setPriority(NORM_PRIORITY - 1);
   	}

   	// Se sobrecarga el metodo run asociado a los threads
	public void run()
	{
		System.out.println(currentThread().toString() + " - " + "Procesando conexion...");

		try
		{
			// Flujo de entrada desde el cliente al servidor
			BufferedReader in = new BufferedReader (new InputStreamReader(scliente.getInputStream()));
			
			// Flujo de salida desde el servidor al cliente
  			out = new PrintWriter(new OutputStreamWriter(scliente.getOutputStream(),"8859_1"),true);
  			
  			// Aca se guarda lo que se lee
  			String cadLeida = "";
  			
  			// Flag
			int i = 0, flag = 0;
	
			// Lee una linea mientras no este vacia y su largo sea diferente de cero
			do{			
			//while(/*leiTodo == 0*/true){
				// Lee una linea
				cadLeida = in.readLine();

				// Si la cadena no es vacia, la imprime en el log
				if(cadLeida != null)
				{
					System.out.println(currentThread().toString() + " - " + "--" + cadLeida + "-");
				}

				// Hace esto solo para la primera linea que se lee; la que tiene el request url
				if(i == 0) 
				{
					// Esto para que se lea solo la primera
			        i++;
			        
			        // StringTokenizer rompe el string en los tokens; st guarda los pedazos de "cadena"
			        StringTokenizer st = new StringTokenizer(cadLeida);
                    
			        // Si se han contado mas de dos tokens y el que sigue es un GET
                    if((st.countTokens() >= 2) && st.nextToken().equals("GET")) 
                    {
                    	// Devuelve el archivo que se pidio en el request
                    	String next = st.nextToken();
                    	//System.out.println(next);
                    	//retornaArchivo(st.nextToken());
                    	retornaArchivo(next);
                    }
                    else 
                    {
                    	// AQUI HACER ELSE IF PARA POST
                    	StringTokenizer stp = new StringTokenizer(cadLeida);
                    	
                    	if((stp.countTokens() >= 2) && stp.nextToken().equals("POST"))
                    	{
                    		String loQueSePidio = st.nextToken();
                    		// Aca deberiamos mandar los datos obtenidos a una funcion que retorne la pagina con la lista
                        	String nombre = "", ip = "", puerto = "", sigueLeyendo = "", mensaje = "", nick = "", de = "", para = "";
                        	int veces = 0;
                        	
                        	if(loQueSePidio.equals("/enviar_msj.html")){
                        		// Caso en que es envio de mensaje
                        		try{
                        			// Se seguira leyendo la peticion hasta terminar de tomar el mensaje
                        			while(true){
                        				sigueLeyendo = in.readLine();
                            			System.out.println(currentThread().toString() + " - " + "--" + sigueLeyendo + "-");
                            			
                            			if(flag == 1)
                            				break;
                            			
                            			if(sigueLeyendo.length() == 0)
                            			{
                            				// Linea por linea tomando los datos ingresados, segun lo dado por enctype="multipart/form-data" 
                            				veces++;
                            				// System.out.println(veces);
                            				if(veces == 2)
                            				{
                            					de = in.readLine();
                            					System.out.println(currentThread().toString() + " - " + "--" + de + "-");
                            				}
                            				else if(veces == 3)
                            				{
                            					para = in.readLine();
                            					System.out.println(currentThread().toString() + " - " + "--" + para + "-");
                            				}
                            				else if(veces == 4)
                            				{
                            					mensaje = in.readLine();
                            					System.out.println(currentThread().toString() + " - " + "--" + mensaje + "-");
                            					flag = 1;
                            				}
                            			}
                        			}
                        			
                        			/****** ESTO ES TAREA 2 ******/
                        			
                        			// Aca se envian los datos al servidor TCP
                        			enviarMsjAServidorTCP(de, para, mensaje);
                        			
                        			/*****************************/
                        		}
                        		catch(Exception exc){
                        			System.out.println(currentThread().toString() + " - " + "Error (en enviar_msj): " + exc.toString());
                        		}
                        	}
                        	else if(loQueSePidio.equals("/recibir_msjs.html")){
                        		// Aca se llega cuando el usuario quiere ver sus mensajes e ingreso su nick
                        		// Entonces en este código agarramos su nick para comparar con los mensajes
                        		// que están en el servidor TCP
                        		try{
                        			while(true){
                        				sigueLeyendo = in.readLine();
                            			System.out.println(currentThread().toString() + " - " + "--" + sigueLeyendo + "-");
                            			
                            			if(flag == 1)
                            				break;
                            			
                            			if(sigueLeyendo.length() == 0)
                            			{
                            				// Linea por linea tomando los datos ingresados, segun lo dado por enctype="multipart/form-data" 
                            				veces++;
                            				// System.out.println(veces);
                            				if(veces == 2)
                            				{
                            					nick = in.readLine();
                            					System.out.println(currentThread().toString() + " - " + "--" + nick + "-");
                            					flag = 1;
                            					//break;
                            				}
                            			}
                        			}
                        			
                        			// En esta parte se envia el nick a una funcion que rellena la pagina de mensajes recibidos
                        			// Con los mensajes que son para el nick especificado
                        			recibirMisMensajes(nick, out);
                        		}
                        		catch(Exception exc){
                        			System.out.println(currentThread().toString() + " - " + "Error (en recibir_msjs): " + exc.toString());
                        		}
                        	}
                        	else{
                        		// Caso en que es /agregar.html
                        		try
                            	{
                            		// Se seguira leyendo la peticion hasta terminar de tomar los datos que se ingresaron
                            		while(true)
                            		{
                            			sigueLeyendo = in.readLine();
                            			System.out.println(currentThread().toString() + " - " + "--" + sigueLeyendo + "-");
                            			
                            			if(flag == 1)
                            				break;
                            			
                            			if(sigueLeyendo.length() == 0)
                            			{
                            				// Linea por linea tomando los datos ingresados, segun lo dado por enctype="multipart/form-data"                            				
                            				veces++;
                            				
                            				if(veces == 2)
                            				{
                            					nombre = in.readLine();
                            					System.out.println(currentThread().toString() + " - " + "--" + nombre + "-");
                            				}                        				
                            				
                            				else if(veces == 3)
                            				{
                            					ip = in.readLine();
                            					System.out.println(currentThread().toString() + " - " + "--" + ip + "-");
                            				}
                            				
                            				else if(veces == 4)
                            				{
                            					puerto = in.readLine();
                            					System.out.println(currentThread().toString() + " - " + "--" + puerto + "-");
                            					flag = 1;
                            					//break;
                            				}
                            			}
                            		}
                            		
                            		// Mandamos a guardar lo que se ha leido
                            		guardarDatosArchivo(nombre, ip, puerto);
                            		
                            		// Aqui se envian los datos obtenidos a una funcion que los escribe en un .txt                        		
                            		leerDatosArchivoYDevolver(out);
                            	}
                            	catch(Exception exc)
                            	{
                            		System.out.println(currentThread().toString() + " - " + "Error: " + exc.toString());
                            	}
                        	}
                        }
                    	else
                    	{
                    		// Si es que no es GET o POST
                    		out.println("400: Solicitud Incorrecta");
                    	}
                    }
				}
				
				if(flag == 1)
				{
					break;
				}
			}
			while (cadLeida != null && cadLeida.length() != 0);
			
			in.close();
			out.close();
			scliente.close();
		}
		catch(Exception e)
		{
			System.out.println(currentThread().toString() + " - " + "Error en servidor: " + e.toString());
		}
		
		System.out.println(currentThread().toString() + " - " + "Fin de la ejecucion");
	}
	
	void enviarMsjAServidorTCP(String de, String para, String mensaje){
		try{
			/***** ENVIO DE DATOS AL SERVIDOR TCP *****/
			
			// Socket que envía hacia el servidor TCP que está escuchando en el puerto 7777
			// y es "localhost" porque está en el mismo computador
			Socket socketCliente = new Socket("localhost", 7777);
			
			// Aca se captura el flujo de datos HACIA el servidor
			DataOutputStream outToServer = new DataOutputStream(socketCliente.getOutputStream());
			
			// Escribimos los datos en el flujo para su envio
			outToServer.writeBytes("PARA," + para + ",De: " + de + " | Mensaje: " + mensaje + '\n');
			
			// Se cierra el socket de cliente creado
			socketCliente.close();
			
			/****************** FIN *******************/
			
			// Se retorna la pagina "agregar.html"
			retornaArchivo("/agregar.html");
		}
		catch(IOException ioe)
		{
			System.out.println(ioe.toString());
			
		}
	}
	
	void recibirMisMensajes(String nick, PrintWriter out){
		// En esta funcion se hace conexion con el servidor para escanear el archivo de mensajes y ver los que le corresponden al nick
		// Se manda una solicitud al servidor, el servidor la procesa y debe enviar de vuelta
		
		String recibidoDesdeServidor = "", mensaje = "";
		
		try{
			// Socket que envía hacia el servidor TCP que está escuchando en el puerto 7777
			// y es "localhost" porque está en el mismo computador
			Socket socketCliente = new Socket("localhost", 7777);
			
			// Aca se captura el flujo de datos HACIA el servidor
			DataOutputStream outToServer = new DataOutputStream(socketCliente.getOutputStream());
			
			// Escribimos los datos en el flujo para su envio
			outToServer.writeBytes("USER " + nick + '\n');
			
			// Aca se captura el flujo DESDE el servidor
			BufferedReader desdeServidorTCP = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));
			
			// Se guarda en recibidoDesdeServidor lo que llego desde el servidor TCP
			recibidoDesdeServidor = desdeServidorTCP.readLine();
			
			// Desde aqui se crea el codigo HTML que tiene los mensajes para el usuario
			out.println("HTTP/1.1 200 OK");
		    out.println("Content-Type: text/html");
		    out.println("\r\n");
		    out.println("<html><head><title>AVIONCITO DE PAPEL</title><style>body{font-family: Tahoma, Geneva, sans-serif;}</style></head><body><h1><center>MENSAJES DE: " + nick + "</center></h1><fieldset>");
		    out.println("<center><table>");
		    out.println("<tr><th> MENSAJES </th></tr>");
							
			StringTokenizer strtok = new StringTokenizer(recibidoDesdeServidor,"--");
			while(strtok.hasMoreTokens())
			{
				mensaje = strtok.nextToken();
				
				// Seguimos imprimiendo la pagina
				out.println("<tr><td>" + mensaje + "</td></tr>");
			}
			
		    out.println("</table></center></fieldset></body></html>");
			
			// Se cierra el socket de cliente creado
			socketCliente.close();
		}
		catch(IOException ioe){
			System.out.println(ioe.toString());
		}
	}
	
	void retornaArchivo(String strArchivo)
	{
		System.out.println(currentThread().toString() + " - " + "Recuperando archivo: " + strArchivo);
		
		// Se revisa si es que tiene una barra
		if (strArchivo.startsWith("/"))
		{
			strArchivo = strArchivo.substring(1) ;
		}
        
        // Si termina en /, le retornamos el HTML en aquel directorio
        // Si la cadena esta vacia, no retorna el HTML de ese directorio
        if (strArchivo.endsWith("/") || strArchivo.equals(""))
        {
        	strArchivo = strArchivo + "agregar.html";
        }
        
        if(strArchivo.equals("lista_contactos.html"))
        {        	
        	try
            {	        
        		leerDatosArchivoYDevolver(out);
    		}
    		catch(Exception e)
    		{
    			System.out.println(currentThread().toString() + " - " + "Error al retornar el archivo");
    		}
        }
        
        else{
	        try
	        {	        
			    // Lectura y retorno del archivo
			    File archPedido = new File(strArchivo);
			        
			    // Si existe el archivo que se pidio
			    if (archPedido.exists()) 
			    {
			    	// Imprime datos de respuesta
		      		out.println("HTTP/1.0 200 ok");
					out.println("Server: ServidorWeb/1.0");
					out.println("Date: " + new Date());
					out.println("Content-Type: text/html");
					out.println("Content-Length: " + archPedido.length());
					out.println("\n");
					
					// El lector se asocia al archivo que se pidio
					BufferedReader archLocal = new BufferedReader(new FileReader(archPedido));
					
					String linea = "";
					
					do			
					{
						linea = archLocal.readLine();
		
						if (linea != null )
						{
							out.println(linea);
						}
					}
					while (linea != null);
					
					// Asi termina si existe el archivo
					System.out.println(currentThread().toString() + " - " + "Envio del archivo finalizado");
					
					archLocal.close();
					out.close();
			    } 
				else
				{
					// Asi termina si no encuentra el archivo
					System.out.println(currentThread().toString() + " - " + "No se encuentra el archivo: " + archPedido.toString());
		      		out.println("HTTP/1.0 400 ok");
		      		out.close();
				}
			}
			catch(Exception e)
			{
				System.out.println(currentThread().toString() + " - " + "Error al retornar el archivo");
			}
        }
	}
	
	void guardarDatosArchivo(String nombre, String ip, String puerto)
	{
		try
		{
			// Se crea el .txt con la lista de contactos
			File listaContactos = new File("lista_contactos.txt");
			
			// El (..., true) es para que se agregue lo leido al final del archivo -append-)
			BufferedWriter escritor = new BufferedWriter(new FileWriter(listaContactos, true));
			
			// Escribo en el archivo nuevo dado el siguiente formato
			if(( nombre == null) || (nombre.isEmpty())){
			nombre =" ";
			System.out.println("campo nombre vacio");
			}
			if(( ip == null) || (ip.isEmpty())){
			ip = "  ";
			System.out.println("campo ip vacio");
			}
			if(( puerto == null) || (puerto.isEmpty())){
			puerto = " ";
			System.out.println("campo ip vacio");
			}
			escritor.write(nombre + "," + ip + "," + puerto);
			escritor.newLine();
				
			// Cierro escritor
			escritor.close();
			
			/***** ENVIO DE DATOS AL SERVIDOR TCP *****/
			// ESTA ES LA PRUEBA QUE HICE DONDE EL PELAO; ENCAPSULÉ LOS DATOS INGRESADOS EN EL FORMULARIO EN UN SÓLO STRING
			// Y LUEGO SE LO ENVIÉ AL SERVIDOR TCP
			// SI SE CAMBIA DE CONSOLA EN ECLIPSE, SE PUEDE VER QUE EL SERVIDOR AVISA QUE LE LLEGÓ ESE STRING
			
			/*
			String datos = nombre + "," + ip + "," + puerto;
			
			Socket socketCliente = new Socket("localhost", 7777);
			DataOutputStream outToServer = new DataOutputStream(socketCliente.getOutputStream());
			outToServer.writeBytes(datos + '\n');
			socketCliente.close();
			*/
			
			/****************** FIN *******************/
		}
		catch(IOException ioe)
		{
			System.out.println(ioe.toString());
		}
	}
	
	void leerDatosArchivoYDevolver(PrintWriter out)
	{
		// En este string va el contenido completo del archivo que contiene la lista
		String contenido = "", nombre, ip, puerto;
		
		// Defino el archivo que voy a abrir
		File archivo = new File("lista_contactos.txt");
		
		try
		{
			// Lector para el archivo
			FileReader lector = new FileReader(archivo);
			BufferedReader buf = new BufferedReader(lector);
			
			out.println("HTTP/1.1 200 OK");
		    out.println("Content-Type: text/html");
		    out.println("\r\n");
		    out.println("<html><head><title>AVIONCITO DE PAPEL</title><style>body{font-family: Tahoma, Geneva, sans-serif;}</style></head><body><h1><center>AVIONCITO DE PAPEL</center></h1><fieldset>");
		    out.println("<center><table>");
		    out.println("<tr><th> NOMBRE </th><th> IP </th><th> PUERTO </th></tr>");
			
			while((contenido = buf.readLine()) != null)
			{
				StringTokenizer strtok = new StringTokenizer(contenido,",");
				while(strtok.hasMoreTokens())
				{
					nombre = strtok.nextToken();
					ip = strtok.nextToken();
					puerto = strtok.nextToken();
					
					// Seguimos imprimiendo la pagina
					out.println("<tr><td>" + nombre + "</td><td>" + ip + "</td><td>" + puerto + "</td></tr>");
				}
			}
			/*
			// Defino un nuevo arreglo de caracteres de largo igual al largo del archivo 
			char[] caracteres = new char[(int)archivo.length()];
			
			// Leo el contenido...
			lector.read(caracteres);
			
			// ...y lo guardo en un string
			contenido = new String(caracteres); */
			
		    out.println("</table></center></fieldset></body></html>");
			
			// Cierra lector
			lector.close();
		}
		catch(IOException ioexc)
		{
			System.out.println(ioexc.toString());
			System.out.println(ioexc.toString());
		}
	}
}
