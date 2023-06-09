package com.protreino.services.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StreamCorruptedException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.codec.binary.Base64;

import com.protreino.services.constants.Tipo;
import com.protreino.services.devices.Device;
import com.protreino.services.devices.FacialDevice;
import com.protreino.services.devices.TopDataDevice;
import com.protreino.services.entity.CartaoComandaEntity;
import com.protreino.services.entity.LogCartaoComandaEntity;
import com.protreino.services.entity.LogPedestrianAccessEntity;
import com.protreino.services.entity.ObjectWithId;
import com.protreino.services.entity.PedestrianAccessEntity;
import com.protreino.services.entity.UserEntity;
import com.protreino.services.enumeration.BroadcastMessageType;
import com.protreino.services.enumeration.NotificationType;
import com.protreino.services.enumeration.StatusCard;
import com.protreino.services.enumeration.TcpMessageType;
import com.protreino.services.enumeration.VerificationResult;
import com.protreino.services.main.Main;
import com.protreino.services.to.BroadcastMessageTO;
import com.protreino.services.to.DeviceTO;
import com.protreino.services.to.InternalLoginResponse;
import com.protreino.services.to.SimpleDevice;
import com.protreino.services.to.SimpleUser;
import com.protreino.services.to.TcpMessageTO;

public class TcpServer {

	private int porta;
	private int portaPdv;

	private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:sss");

	public TcpServer() {
		this.porta = Integer.valueOf(Utils.getPreference("tcpServerSocketPort"));
		this.portaPdv = 2021;

		Thread serverThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try (ServerSocket serverSocket = new ServerSocket(porta)) {
					System.out.println(sdf.format(new Date()) + "  ... TCP server escutando na porta " + porta);

					while (true) {
						Socket socket = serverSocket.accept();
						socket.setTcpNoDelay(true);
						System.out.println(sdf.format(new Date()) + "  ... New client connected: "
								+ socket.getInetAddress().getHostAddress());
						new ProcessThread(socket).start();
					}

				} catch (IOException ex) {
					System.out.println(sdf.format(new Date()) + "  ... TCP server exception: " + ex.getMessage());
					ex.printStackTrace();
				}
			}
		});
		serverThread.start();

		Thread serverThreadPDV = new Thread(new Runnable() {
			@Override
			public void run() {
				try (ServerSocket serverSocket = new ServerSocket(portaPdv)) {
					System.out.println(sdf.format(new Date()) + "  ... TCP PDV server escutando na porta " + portaPdv);

					while (true) {
						Socket socket = serverSocket.accept();
						socket.setTcpNoDelay(true);
						System.out.println(sdf.format(new Date()) + "  ... New PDV client connected: "
								+ socket.getInetAddress().getHostAddress());
						new ProcessPdvThread(socket).start();
					}

				} catch (IOException ex) {
					System.out.println(sdf.format(new Date()) + "  ... TCP server exception: " + ex.getMessage());
					ex.printStackTrace();
				}
			}
		});
		serverThreadPDV.start();

	}

	public class ProcessThread extends Thread {
		private Socket socket;

		public ProcessThread(Socket socket) {
			this.socket = socket;
		}

		public void run() {
			PrintWriter outToClient = null;
			ObjectInputStream inFromClient = null;

			try {

				inFromClient = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
				TcpMessageTO receivedTcpMessage;
				Object received;

				while (inFromClient != null && (received = (Object) inFromClient.readObject()) != null) {

					if (received instanceof TcpMessageTO) {
						/*
						 * tratamento de mensagens do sistemas
						 */
						receivedTcpMessage = (TcpMessageTO) received;
						TcpMessageTO responseTcpMessage = new TcpMessageTO();
						responseTcpMessage.setParans(new HashMap<String, Object>());

						if (TcpMessageType.PROCESS_ACCESS_REQUEST.equals(receivedTcpMessage.getType())) {
							Object[] object = processAccessRequest(receivedTcpMessage);
							responseTcpMessage.getParans().put("object", object);
						} else if (TcpMessageType.PROCESS_ACCESS_REQUEST_2.equals(receivedTcpMessage.getType())) {
							Object[] object = processAccessRequest2(receivedTcpMessage);
							responseTcpMessage.getParans().put("object", object);

						} else if (TcpMessageType.GET_RESULT_LIST.equals(receivedTcpMessage.getType())) {
							List<?> list = getResultList(receivedTcpMessage);
							responseTcpMessage.getParans().put("list", list);

						} else if (TcpMessageType.GET_RESULT_LIST_LIMITED.equals(receivedTcpMessage.getType())) {
							List<?> list = getResultListLimited(receivedTcpMessage);
							responseTcpMessage.getParans().put("list", list);

						} else if (TcpMessageType.GET_RESULT_LIST_COUNT.equals(receivedTcpMessage.getType())) {
							Integer count = getResultListCount(receivedTcpMessage);
							responseTcpMessage.getParans().put("count", count);

						} else if (TcpMessageType.GET_RESULT_LIST_WITH_PARAMS.equals(receivedTcpMessage.getType())) {
							List<?> list = getResultListWithParams(receivedTcpMessage);
							responseTcpMessage.getParans().put("list", list);

						} else if (TcpMessageType.GET_RESULT_LIST_WITH_PARAMS_COUNT
								.equals(receivedTcpMessage.getType())) {
							Integer count = getResultListWithParamsCount(receivedTcpMessage);
							responseTcpMessage.getParans().put("count", count);

						} else if (TcpMessageType.GET_UNIQUE_RESULT_WITH_PARAMS.equals(receivedTcpMessage.getType())) {
							Object object = getUniqueResultWithParams(receivedTcpMessage);
							responseTcpMessage.getParans().put("object", object);

						} else if (TcpMessageType.GET_RESULT_LIST_WITH_DYNAMIC_PARAMS
								.equals(receivedTcpMessage.getType())) {
							List<?> list = getResultListWithDynamicParams(receivedTcpMessage);
							responseTcpMessage.getParans().put("list", list);

						} else if (TcpMessageType.GET_RESULT_LIST_WITH_DYNAMIC_PARAMS_COUNT
								.equals(receivedTcpMessage.getType())) {
							Integer count = getResultListWithDynamicParamsCount(receivedTcpMessage);
							responseTcpMessage.getParans().put("count", count);

						} else if (TcpMessageType.GET_SINGLE_RESULT.equals(receivedTcpMessage.getType())) {
							Object object = getSingleResult(receivedTcpMessage);
							responseTcpMessage.getParans().put("object", object);

						} else if (TcpMessageType.COUNT_ACESSOS_PEDESTRE.equals(receivedTcpMessage.getType())) {
							Long qtdeAcessos = countAcessosPedestre(receivedTcpMessage);
							responseTcpMessage.getParans().put("qtdeAcessos", qtdeAcessos);

						} else if (TcpMessageType.GET_SINGLE_RESULT_BY_ID.equals(receivedTcpMessage.getType())) {
							Object object = getSingleResultById(receivedTcpMessage);
							responseTcpMessage.getParans().put("object", object);

						} else if (TcpMessageType.GET_SINGLE_RESULT_BY_CARD_NUMBER
								.equals(receivedTcpMessage.getType())) {
							Object object = getSingleResultByCardNumber(receivedTcpMessage);
							responseTcpMessage.getParans().put("object", object);

						} else if (TcpMessageType.GET_SINGLE_RESULT_BY_RG.equals(receivedTcpMessage.getType())) {
							Object object = getSingleResultByRG(receivedTcpMessage);
							responseTcpMessage.getParans().put("object", object);

						} else if (TcpMessageType.GET_SINGLE_RESULT_BY_CPF.equals(receivedTcpMessage.getType())) {
							Object object = getSingleResultByCPF(receivedTcpMessage);
							responseTcpMessage.getParans().put("object", object);

						} else if (TcpMessageType.SAVE.equals(receivedTcpMessage.getType())) {
							Object[] object = save(receivedTcpMessage);
							responseTcpMessage.getParans().put("object", object);

						} else if (TcpMessageType.UPDATE.equals(receivedTcpMessage.getType())) {
							Object[] object = update(receivedTcpMessage);
							responseTcpMessage.getParans().put("object", object);

						} else if (TcpMessageType.REMOVE.equals(receivedTcpMessage.getType())) {
							remove(receivedTcpMessage);

						} else if (TcpMessageType.REMOVE_TEMPLATES.equals(receivedTcpMessage.getType())) {
							removeTemplates(receivedTcpMessage);

						} else if (TcpMessageType.IS_ANIVERSARIANTE.equals(receivedTcpMessage.getType())) {
							Boolean isAniversariante = HibernateUtil.isAniversariante();
							responseTcpMessage.getParans().put("isAniversariante", isAniversariante);

						} else if (TcpMessageType.GET_MATCHED_ATHLETE_ACCESS.equals(receivedTcpMessage.getType())) {
							PedestrianAccessEntity pedestre = HibernateUtil.getMatchedAthleteAccess();
							responseTcpMessage.getParans().put("pedestre", pedestre);

						} else if (TcpMessageType.BUSCA_USUARIO_PELO_LOGIN.equals(receivedTcpMessage.getType())) {
							UserEntity user = buscaUsuarioPeloLogin(receivedTcpMessage);
							responseTcpMessage.getParans().put("user", user);

						} else if (TcpMessageType.GET_SINGLE_RESULT_BY_REGISTRATION
								.equals(receivedTcpMessage.getType())) {
							Object object = getSingleResultByRegistration(receivedTcpMessage);
							responseTcpMessage.getParans().put("object", object);

						} else if (TcpMessageType.IS_PASTA_DE_FOTOS_EXISTENTE.equals(receivedTcpMessage.getType())) {
							Boolean resp = isPastaDeFotosExistente(receivedTcpMessage);
							responseTcpMessage.getParans().put("resp", resp);

						} else if (TcpMessageType.APAGAR_PASTA_DE_FOTOS.equals(receivedTcpMessage.getType())) {
							apagarPastaDeFotos(receivedTcpMessage);

						} else if (TcpMessageType.UPLOAD_FOTOS_PARA_SERVIDOR_DE_RECONHECIMENTO
								.equals(receivedTcpMessage.getType())) {
							salvarFotosNaMaquina(receivedTcpMessage);

						} else if (TcpMessageType.REGISTRA_NOVAS_FOTOS_PEDESTRE.equals(receivedTcpMessage.getType())) {
							registraNovasFotosPedestre(receivedTcpMessage);

						} else if (TcpMessageType.REGISTRA_EXCLUSAO_FOTOS_PEDESTRE
								.equals(receivedTcpMessage.getType())) {
							registraExclusaoFotosPedestre(receivedTcpMessage);

						} else if (TcpMessageType.INTERNAL_LOGIN_REQUEST.equals(receivedTcpMessage.getType())) {
							InternalLoginResponse response = doInternalLogin(receivedTcpMessage);
							responseTcpMessage.getParans().put("response", response);

						} else if (TcpMessageType.GET_TICKET_GATE_LIST.equals(receivedTcpMessage.getType())) {
							List<SimpleDevice> list = getTicketGateList(receivedTcpMessage);
							responseTcpMessage.getParans().put("list", list);

						} else if (TcpMessageType.PROCESS_ACCESS_REQUEST_FROM_APP
								.equals(receivedTcpMessage.getType())) {
							VerificationResult verificationResult = processAccessRequestFromApp(receivedTcpMessage);
							responseTcpMessage.getParans().put("verification_result", verificationResult);

						} else if (TcpMessageType.PING.equals(receivedTcpMessage.getType())) {
							responseTcpMessage.setType(TcpMessageType.PING_RESPONSE);

						} else if (TcpMessageType.ACCESS_VALIDATING_INI.equals(receivedTcpMessage.getType())) {
							Main.validandoAcesso = true;
							responseTcpMessage.setType(TcpMessageType.PING_RESPONSE);

						} else if (TcpMessageType.ACCESS_VALIDATING_FIM.equals(receivedTcpMessage.getType())) {
							Main.validandoAcesso = false;
							responseTcpMessage.setType(TcpMessageType.PING_RESPONSE);

						} else if (TcpMessageType.RESET_STATUS_ALL_CARDS.equals(receivedTcpMessage.getType())) {
							responseTcpMessage.setType(TcpMessageType.RESET_STATUS_ALL_CARDS);
							HibernateUtil.resetStatusAllCards();

						} else if (TcpMessageType.GET_ALL_PEDESTRIAN_BY_ID.equals(receivedTcpMessage.getType())) {
							Object object = getAllPedestrianById(receivedTcpMessage);
							responseTcpMessage.getParans().put("object", object);

						} else if (TcpMessageType.GET_DEVICES_FROM_SERVER.equals(receivedTcpMessage.getType())) {
							List<?> list = getDevices();
							responseTcpMessage.getParans().put("list", list);
						} else if (TcpMessageType.LIBERAR_ACESSO_DEVICE_NO_SERVIDOR
								.equals(receivedTcpMessage.getType())) {
							liberarDevice(receivedTcpMessage);

						} else if (TcpMessageType.GET_ALL_DEVICES_FROM_SERVER
								.equals(receivedTcpMessage.getType())) {
							List<?> list = getDevicesListServer(receivedTcpMessage);
							responseTcpMessage.getParans().put("list", list);
						}
						else {
							responseTcpMessage.setType(TcpMessageType.ERROR);
						}

						ObjectOutputStream outputWriter = new ObjectOutputStream(
								new BufferedOutputStream(socket.getOutputStream()));
						outputWriter.writeObject(responseTcpMessage);
						outputWriter.flush();

					}
				}
			} catch (EOFException eof) {
				eof.printStackTrace();
			} catch (SocketException se) {
				se.printStackTrace();
			} catch (Exception e) {
				System.out.println(sdf.format(new Date()) + "  ... TCP server exception: " + e.getMessage());
				e.printStackTrace();

			} finally {
				try {
					if (inFromClient != null)
						inFromClient.close();
					if (outToClient != null)
						outToClient.close();
				} catch (Exception e) {
				}
			}
		}

		private void liberarDevice(TcpMessageTO receivedTcpMessage) {
			String indentifier = (String) receivedTcpMessage.getParans().get("indentifier");
			String sentido = (String) receivedTcpMessage.getParans().get("sentido");
			Device device = HibernateUtil.getDeviceByIdentifier(indentifier);
			
			if (Tipo.ENTRADA.equals(sentido)) {
				allowAccess(device, sentido);
			} else if (Tipo.SAIDA.equals(sentido)) {
				Main.apertouF10 = true;
				allowAccess(device, sentido);
				Main.apertouF10 = false;
			}
		}

		private void allowAccess(Device device, String sentido) {
			LogPedestrianAccessEntity logAccess = new LogPedestrianAccessEntity(Main.loggedUser.getId(), null,
					"LIBERADO DO CLIENTE PARA O SERVIDOR", device.getLocation(), "LIBERADO PELO SISTEMA");
			logAccess.setDirection(sentido);
			Utils.createNotification("Acesso liberado pelo sistema.", NotificationType.GOOD);
			HibernateUtil.save(LogPedestrianAccessEntity.class, logAccess);
			
			if (Main.broadcastServer != null) {
				Main.broadcastServer.sendMessage(new BroadcastMessageTO(BroadcastMessageType.LOG_ACCESS, logAccess));
			}
			
			device.setVerificationResult(VerificationResult.ALLOWED);
			device.allowAccess();
		}

		private List<?> getDevices() {
			List<DeviceTO> devices = new ArrayList<>();
			if (Main.devicesList != null && !Main.devicesList.isEmpty()) {
				for (Device device : Main.devicesList) {
					if (device instanceof TopDataDevice) {
						DeviceTO deviceTO = new DeviceTO(device);
						devices.add(deviceTO);
					}
				}

			}
			return devices;
		}
		
		private List<?> getDevicesGromServer() {
			List<Device> devices = new ArrayList<>();
			if (Main.devicesList != null && !Main.devicesList.isEmpty()) {
				for (Device device : Main.devicesList) {
					if (device instanceof TopDataDevice) {
						devices.add(device);
					}
				}

			}
			return devices;
		} 

		private Object getSingleResultByCPF(TcpMessageTO receivedTcpMessage) throws ClassNotFoundException {
			String classe = (String) receivedTcpMessage.getParans().get("entityClass");
			Class<?> entityClass = Class.forName(classe);
			String cpf = (String) receivedTcpMessage.getParans().get("cpf");

			return HibernateUtil.getSingleResultByCPF(entityClass, cpf);
		}

		private Object getSingleResultByRG(TcpMessageTO receivedTcpMessage) throws ClassNotFoundException {
			String classe = (String) receivedTcpMessage.getParans().get("entityClass");
			Class<?> entityClass = Class.forName(classe);
			String rg = (String) receivedTcpMessage.getParans().get("rg");

			return HibernateUtil.getSingleResultByRG(entityClass, rg);
		}

		private void registraExclusaoFotosPedestre(TcpMessageTO receivedTcpMessage) {
			Long idUsuario = (Long) receivedTcpMessage.getParans().get("idUsuario");

			HibernateUtil.registraExclusaoFotosPedestre(idUsuario);
		}

		private void registraNovasFotosPedestre(TcpMessageTO receivedTcpMessage) {
			Long idUsuario = (Long) receivedTcpMessage.getParans().get("idUsuario");

			HibernateUtil.registraNovasFotosPedestre(idUsuario);
		}

		private void salvarFotosNaMaquina(TcpMessageTO receivedTcpMessage) {
			String zipFileByteArray = (String) receivedTcpMessage.getParans().get("zipFileByteArray");
			Long idUsuario = (Long) receivedTcpMessage.getParans().get("idUsuario");

			byte[] photos = null;
			if (zipFileByteArray != null)
				photos = Base64.decodeBase64(zipFileByteArray);

			if (photos != null) {
				String saveZipPath = Utils.getAppDataFolder() + "/reconhecimento_facial/fotos/" + idUsuario + ".zip";
				String folderPath = Utils.getAppDataFolder() + "/reconhecimento_facial/fotos/" + idUsuario + "/";

				String path = Utils.getAppDataFolder() + "/reconhecimento_facial/fotos/";
				File diretorioFotos = new File(path);
				if (!diretorioFotos.exists())
					diretorioFotos.mkdirs();

				try (FileOutputStream fos = new FileOutputStream(saveZipPath)) {
					fos.write(photos);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				Main.unzip(saveZipPath, folderPath);

				Main.apagarArquivo(saveZipPath);
			}
		}

		private void apagarPastaDeFotos(TcpMessageTO receivedTcpMessage) {
			Long idUser = (Long) receivedTcpMessage.getParans().get("idUser");

			HibernateUtil.apagarPastaDeFotos(idUser);
		}

		private Boolean isPastaDeFotosExistente(TcpMessageTO receivedTcpMessage) {
			Integer idUser = (Integer) receivedTcpMessage.getParans().get("idUser");

			return HibernateUtil.isPastaDeFotosExistente(idUser);
		}

		private <T> Object getSingleResultByRegistration(TcpMessageTO receivedTcpMessage)
				throws ClassNotFoundException {
			String classe = (String) receivedTcpMessage.getParans().get("entityClass");
			Class<?> classeEntidade = Class.forName(classe);
			Long cardNumber = (Long) receivedTcpMessage.getParans().get("cardNumber");

			return HibernateUtil.getSingleResultByRegistration(classeEntidade, cardNumber);
		}

		private Object[] processAccessRequest(TcpMessageTO receivedTcpMessage) {
			String codigo = (String) receivedTcpMessage.getParans().get("codigo");
			String location = (String) receivedTcpMessage.getParans().get("location");
			boolean ignoraRegras = (boolean) receivedTcpMessage.getParans().get("ignoraRegras");
			boolean createNotification = (boolean) receivedTcpMessage.getParans().get("createNotification");

			return HibernateUtil.processAccessRequest(codigo, location, createNotification, ignoraRegras);
		}

		private Object[] processAccessRequest2(TcpMessageTO receivedTcpMessage) {
			String codigo = (String) receivedTcpMessage.getParans().get("codigo");
			String equipamento = (String) receivedTcpMessage.getParans().get("equipamento");
			Integer origem = (Integer) receivedTcpMessage.getParans().get("origem");
			String location = (String) receivedTcpMessage.getParans().get("location");
			boolean usaUrna = (boolean) receivedTcpMessage.getParans().get("usaUrna");
			boolean createNotification = (boolean) receivedTcpMessage.getParans().get("createNotification");
			boolean ignoraRegras = (boolean) receivedTcpMessage.getParans().get("ignoraRegras");
			Date data = null;
			try {
				data = (Date) receivedTcpMessage.getParans().get("data");
			} catch (Exception e) {
			}
			Boolean digitaisCatraca = Boolean.FALSE;
			try {
				digitaisCatraca = (Boolean) receivedTcpMessage.getParans().get("digitaisCatraca");
			} catch (Exception e) {
			}

			return HibernateUtil.processAccessRequest(codigo, equipamento, origem, location, usaUrna,
					createNotification, data, digitaisCatraca, ignoraRegras);
		}

		private UserEntity buscaUsuarioPeloLogin(TcpMessageTO receivedTcpMessage) {
			String loginName = (String) receivedTcpMessage.getParans().get("loginName");
			String password = (String) receivedTcpMessage.getParans().get("password");

			return HibernateUtil.buscaUsuarioPeloLogin(loginName, password);
		}

		private void removeTemplates(TcpMessageTO receivedTcpMessage) {
			Long idAthleteAccess = (Long) receivedTcpMessage.getParans().get("idAthleteAccess");

			HibernateUtil.removeTemplates(idAthleteAccess);
		}

		private void remove(TcpMessageTO receivedTcpMessage) {
			Object object = receivedTcpMessage.getParans().get("object");

			HibernateUtil.remove(object);
		}

		private Object[] update(TcpMessageTO receivedTcpMessage) throws ClassNotFoundException {
			String classe = (String) receivedTcpMessage.getParans().get("classeEntidade");

			Class<?> classeEntidade = Class.forName(classe);
			ObjectWithId object = (ObjectWithId) receivedTcpMessage.getParans().get("object");

			return HibernateUtil.update(classeEntidade, object);
		}

		private Object[] save(TcpMessageTO receivedTcpMessage) throws ClassNotFoundException {
			String classe = (String) receivedTcpMessage.getParans().get("classeEntidade");
			Class<?> classeEntidade = Class.forName(classe);
			ObjectWithId object = (ObjectWithId) receivedTcpMessage.getParans().get("object");

			return HibernateUtil.save(classeEntidade, object);
		}

		private Object getSingleResultByCardNumber(TcpMessageTO receivedTcpMessage) throws ClassNotFoundException {
			String classe = (String) receivedTcpMessage.getParans().get("entityClass");
			Class<?> entityClass = Class.forName(classe);
			Long cardNumber = (Long) receivedTcpMessage.getParans().get("cardNumber");

			return HibernateUtil.getSingleResultByCardNumber(entityClass, cardNumber);
		}

		private Object getSingleResultById(TcpMessageTO receivedTcpMessage) throws ClassNotFoundException {
			String classe = (String) receivedTcpMessage.getParans().get("entityClass");
			Class<?> entityClass = Class.forName(classe);
			Long id = (Long) receivedTcpMessage.getParans().get("id");

			return HibernateUtil.getSingleResultById(entityClass, id);
		}

		private Object getAllPedestrianById(TcpMessageTO receivedTcpMessage) throws ClassNotFoundException {
			Long id = (Long) receivedTcpMessage.getParans().get("id");

			return HibernateUtil.getAllPedestresById(id);
		}

		private Long countAcessosPedestre(TcpMessageTO receivedTcpMessage) {
			Long idPedestre = (Long) receivedTcpMessage.getParans().get("idPedestre");

			return HibernateUtil.countAcessosPedestre(idPedestre);
		}

		private Object getSingleResult(TcpMessageTO receivedTcpMessage) throws ClassNotFoundException {
			String classe = (String) receivedTcpMessage.getParans().get("entityClass");
			Class<?> entityClass = Class.forName(classe);
			String namedQuery = (String) receivedTcpMessage.getParans().get("namedQuery");

			return HibernateUtil.getSingleResult(entityClass, namedQuery);
		}

		private List<?> getResultListWithDynamicParams(TcpMessageTO receivedTcpMessage) throws ClassNotFoundException {
			String classe = (String) receivedTcpMessage.getParans().get("entityClass");
			Class<?> entityClass = Class.forName(classe);
			String construtor = (String) receivedTcpMessage.getParans().get("construtor");
			String join = (String) receivedTcpMessage.getParans().get("join");
			String groupBy = (String) receivedTcpMessage.getParans().get("groupBy");
			String orderColumn = (String) receivedTcpMessage.getParans().get("orderColumn");

			@SuppressWarnings("unchecked")
			HashMap<String, Object> args = (HashMap<String, Object>) receivedTcpMessage.getParans().get("args");

			Integer inicio = (Integer) receivedTcpMessage.getParans().get("inicio");
			Integer quantidade = (Integer) receivedTcpMessage.getParans().get("quantidade");

			return HibernateUtil.getResultListWithDynamicParams(entityClass, construtor, join, groupBy, orderColumn,
					args, inicio, quantidade);
		}

		private Integer getResultListWithDynamicParamsCount(TcpMessageTO receivedTcpMessage)
				throws ClassNotFoundException {
			String classe = (String) receivedTcpMessage.getParans().get("entityClass");
			Class<?> entityClass = Class.forName(classe);
			String construtor = (String) receivedTcpMessage.getParans().get("construtor");
			String join = (String) receivedTcpMessage.getParans().get("join");
			String groupBy = (String) receivedTcpMessage.getParans().get("groupBy");

			@SuppressWarnings("unchecked")
			HashMap<String, Object> args = (HashMap<String, Object>) receivedTcpMessage.getParans().get("args");

			return HibernateUtil.getResultListWithDynamicParamsCount(entityClass, construtor, join, groupBy, args);
		}

		private Object getUniqueResultWithParams(TcpMessageTO receivedTcpMessage) throws ClassNotFoundException {
			String namedQuery = (String) receivedTcpMessage.getParans().get("namedQuery");
			String classe = (String) receivedTcpMessage.getParans().get("entityClass");
			Class<?> entityClass = Class.forName(classe);

			@SuppressWarnings("unchecked")
			HashMap<String, Object> args = (HashMap<String, Object>) receivedTcpMessage.getParans().get("args");

			return HibernateUtil.getUniqueResultWithParams(entityClass, namedQuery, args);
		}

		private List<?> getResultList(TcpMessageTO receivedTcpMessage) throws ClassNotFoundException {
			String namedQuery = (String) receivedTcpMessage.getParans().get("namedQuery");
			String classe = (String) receivedTcpMessage.getParans().get("entityClass");
			Class<?> entityClass = Class.forName(classe);

			return HibernateUtil.getResultList(entityClass, namedQuery);
		}
		
		private List<?> getDevicesListServer(TcpMessageTO receivedTcpMessage) throws ClassNotFoundException {
			String namedQuery = (String) receivedTcpMessage.getParans().get("namedQuery");
			String classe = (String) receivedTcpMessage.getParans().get("entityClass");
			Class<?> entityClass = Class.forName(classe);

			return HibernateUtil.buscaListaDevicesDoServidor(entityClass, namedQuery);
		}

		private Integer getResultListCount(TcpMessageTO receivedTcpMessage) throws ClassNotFoundException {
			String namedQuery = (String) receivedTcpMessage.getParans().get("namedQuery");
			String classe = (String) receivedTcpMessage.getParans().get("entityClass");			
			Class<?> entityClass = Class.forName(classe);

			@SuppressWarnings("unchecked")
			HashMap<String, Object> args = (HashMap<String, Object>) receivedTcpMessage.getParans().get("args");

			return HibernateUtil.getResultListCount(entityClass, namedQuery, args);
		}

		private List<?> getResultListLimited(TcpMessageTO receivedTcpMessage) throws ClassNotFoundException {
			String namedQuery = (String) receivedTcpMessage.getParans().get("namedQuery");
			String classe = (String) receivedTcpMessage.getParans().get("entityClass");
			Long quantidade = (Long) receivedTcpMessage.getParans().get("quantidade");
			Class<?> entityClass = Class.forName(classe);

			return HibernateUtil.getResultListLimited(entityClass, namedQuery, quantidade);
		}

		private List<?> getResultListWithParams(TcpMessageTO receivedTcpMessage) throws ClassNotFoundException {
			String namedQuery = (String) receivedTcpMessage.getParans().get("namedQuery");
			String classe = (String) receivedTcpMessage.getParans().get("entityClass");
			Class<?> entityClass = Class.forName(classe);

			@SuppressWarnings("unchecked")
			HashMap<String, Object> args = (HashMap<String, Object>) receivedTcpMessage.getParans().get("args");

			Integer inicio = (Integer) receivedTcpMessage.getParans().get("inicio");
			Integer quantidade = (Integer) receivedTcpMessage.getParans().get("quantidade");

			return HibernateUtil.getResultListWithParams(entityClass, namedQuery, args, inicio, quantidade);
		}

		private Integer getResultListWithParamsCount(TcpMessageTO receivedTcpMessage) throws ClassNotFoundException {
			String namedQuery = (String) receivedTcpMessage.getParans().get("namedQuery");
			String classe = (String) receivedTcpMessage.getParans().get("entityClass");
			Class<?> entityClass = Class.forName(classe);

			@SuppressWarnings("unchecked")
			HashMap<String, Object> args = (HashMap<String, Object>) receivedTcpMessage.getParans().get("args");

			return HibernateUtil.getResultListWithParamsCount(entityClass, namedQuery, args);
		}

		private InternalLoginResponse doInternalLogin(TcpMessageTO receivedTcpMessage) {

			InternalLoginResponse response = new InternalLoginResponse(false);

			try {
				String username = (String) receivedTcpMessage.getParans().get("username");
				String password = (String) receivedTcpMessage.getParans().get("password");

				UserEntity usuario = HibernateUtil.buscaUsuarioPeloLogin(username, password);

				if (usuario != null) {
					response.setSuccess(true);
					response.setUser(new SimpleUser(usuario.getId(), usuario.getName(), usuario.getIdClient()));
				} else {
					response.setErrorMessage("Dados inválidos!");
				}

			} catch (Exception e) {
				e.printStackTrace();
				response.setErrorMessage("Erro no servidor. " + e.getMessage());
			}

			return response;
		}

		private List<SimpleDevice> getTicketGateList(TcpMessageTO receivedTcpMessage) {
			List<SimpleDevice> devices = new ArrayList<SimpleDevice>();
			for (Device device : Main.devicesList) {
				if (device.isCatraca()) {
					devices.add(new SimpleDevice(device.getName(), device.getIdentifier()));
				}
			}
			return devices;
		}

		private VerificationResult processAccessRequestFromApp(TcpMessageTO receivedTcpMessage) {
			String deviceIdentifier = (String) receivedTcpMessage.getParans().get("device_identifier");
			String codigo = (String) receivedTcpMessage.getParans().get("codigo");
			String location = (String) receivedTcpMessage.getParans().get("location");
			Boolean createNotification = (Boolean) receivedTcpMessage.getParans().get("createNotification");
			boolean ignoraRegras = false;
			try {
				ignoraRegras = (boolean) receivedTcpMessage.getParans().get("ignoraRegras");
			} catch (Exception e) {
			}
			System.out.println("Recebido código: " + codigo);
			System.out.println("Recebido catraca: " + deviceIdentifier);

			VerificationResult verificationResult = VerificationResult.NOT_FOUND;
			Device device = null;

			// procura catraca associada
			if (Main.devicesList != null && !Main.devicesList.isEmpty()) {
				for (Device d : Main.devicesList) {
					if (d.getIdentifier().equals(deviceIdentifier)) {
						device = d;
						break;
					}
				}
			}

			if (device != null && device.isConnected()) {

				System.out.println("Libera na catraca: " + deviceIdentifier);
				String idEquipamento = device instanceof TopDataDevice ? "Inner " + device.getIdentifier().split(";")[0]
						: device.getIdentifier();

				Object[] retorno = HibernateUtil.processAccessRequest(codigo, idEquipamento, FacialDevice.ORIGEM_FACIAL,
						location, false, createNotification, ignoraRegras);
				Utils.sleep(500);

				verificationResult = (VerificationResult) retorno[0];
				device.setVerificationResult(verificationResult);
				device.setAllowedUserName((String) retorno[1]);
				device.setMatchedFacialId(((PedestrianAccessEntity) retorno[2]).getId());

				System.out.println("Resultado na análise: " + verificationResult);
				if (VerificationResult.ALLOWED.equals(verificationResult)
						|| VerificationResult.TOLERANCE_PERIOD.equals(verificationResult)) {
					device.allowAccess((PedestrianAccessEntity) retorno[2]);
				} else if (!VerificationResult.NOT_FOUND.equals(verificationResult)) {
					device.denyAccess();
				}
			} else {
				System.out.println("Libera sem catraca");
				// se não tiver, realiza um processo de liberação genêrico
				Object[] retorno = HibernateUtil.processAccessRequest(codigo, location, createNotification,
						ignoraRegras);
				if (retorno != null && retorno.length > 0)
					verificationResult = (VerificationResult) retorno[0];
			}

			return verificationResult;
		}

	}

	public class ProcessPdvThread extends Thread {
		private Socket socket;

		public ProcessPdvThread(Socket socket) {
			this.socket = socket;
		}

		public void run() {
			PrintWriter outToClient = null;
			BufferedReader inFromClient = null;

			try {

				inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String receivedData = "";
				String responseData = "";

				while (socket.isConnected()) {
					receivedData = inFromClient.readLine();

					if (receivedData != null) {
						System.out.println("data: {}" + receivedData);
						responseData = processaCartaoComanda(receivedData, responseData);

						PrintWriter outputWriter = new PrintWriter(socket.getOutputStream(), true);
						outputWriter.write(responseData);
						outputWriter.flush();
						break;
					}
				}

			} catch (EOFException eof) {
				eof.printStackTrace();
			} catch (SocketException se) {
				se.printStackTrace();
			} catch (Exception e) {
				System.out.println(sdf.format(new Date()) + "  ... TCP server exception: " + e.getMessage());
				e.printStackTrace();

			} finally {
				try {
					if (inFromClient != null)
						inFromClient.close();
					if (outToClient != null)
						outToClient.close();
				} catch (Exception e) {
				}
			}

		}

		private String processaCartaoComanda(String receivedData, String responseData) {
			// para liberação de cartões
			if (receivedData != null) {

				if (receivedData.startsWith("SIER;STCSI;2")) {
					// teste feito pelo PDV
					responseData = "SIER;STCSI;2;21|800;0";

				} else if (receivedData.startsWith("SIER;BLC;")) {
					responseData = processaLiberacaoCartaoComanda(receivedData);

				} else if (receivedData.startsWith("SIER;SSIC;")) {
					responseData = processaStatusCartaoComanda(receivedData);

				}
			}
			return responseData;
		}

		private String processaStatusCartaoComanda(String receivedData) {
			String response = receivedData + ";103";// inicia com mensagem de erro
			String[] parts = receivedData.split(";");
			try {
				CartaoComandaEntity cartao = recuperaCartaoComanda(parts[2]);
				if (cartao != null) {
					// encontrado
					response = receivedData + (StatusCard.LIBERADO.equals(cartao.getStatus()) ? ";1" : ";2");
				} else {
					// não encontrado
					response = receivedData + ";101";
				}
			} catch (Exception e) {
				e.printStackTrace();
				response = receivedData + ";102";
			}
			return response;
		}

		private String processaLiberacaoCartaoComanda(String receivedData) {
			String response = receivedData + ";103";// inicia com mensagem de erro
			String[] parts = receivedData.split(";");
			try {
				CartaoComandaEntity cartao = recuperaCartaoComanda(parts[2]);
				if (cartao != null) {
					// encontrado
					if (parts[3].equals("0")) {
						alteraStatusCartaoComanda(cartao, StatusCard.LIBERADO);
						response = receivedData + ";0";
					} else if (parts[3].equals("1")) {
						alteraStatusCartaoComanda(cartao, StatusCard.BLOQUEADO);
						response = receivedData + ";102";
					}
				} else {
					// não encontrado
					response = receivedData + ";101";
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return response;
		}

		@SuppressWarnings("unchecked")
		public CartaoComandaEntity recuperaCartaoComanda(String numero) {
			HashMap<String, Object> args = new HashMap<>();
			args.put("numeroAlternativo", numero);
			args.put("removido", false);
			List<CartaoComandaEntity> cartoes = (List<CartaoComandaEntity>) HibernateUtil
					.getResultListWithDynamicParams(CartaoComandaEntity.class, null, args);

			if (cartoes != null && !cartoes.isEmpty()) {
				CartaoComandaEntity matched = null;
				if (cartoes != null && !cartoes.isEmpty()) {
					if (cartoes.size() > 1) {
						for (CartaoComandaEntity c : cartoes) {
							if (c.getRemovido() == null || Boolean.FALSE.equals(c.getRemovido())) {
								matched = c;
								break;
							}
						}
					} else
						matched = cartoes.get(0);
				}
				return matched;
			}

			return null;
		}

		private void alteraStatusCartaoComanda(CartaoComandaEntity cartao, StatusCard status) {
			cartao.setStatus(status);
			cartao.setDataAlteracao(new Date());
			HibernateUtil.update(CartaoComandaEntity.class, cartao);

			LogCartaoComandaEntity log = new LogCartaoComandaEntity(cartao);
			log.setUsuario(Main.internoLoggedUser);
			log.setTipoLiberacao("AUTOMATICA_" + status.name());
			log.setOrigem("API");
			log.setData(new Date());
			HibernateUtil.save(LogCartaoComandaEntity.class, log);

		}

	}

}
