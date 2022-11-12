package com.protreino.services.to;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DetectFaceResult {
	
	private int resultCode;
	private String resultDescription;
	private Face face;
	
	public DetectFaceResult() {
		this.resultCode = -1;
		this.resultDescription = errors.getOrDefault(resultCode, String.valueOf(resultCode));
	}
	
	public int getResultCode() {
		return resultCode;
	}
	
	public void setResultCode(int result) {
		this.resultCode = result;
		this.resultDescription = errors.getOrDefault(resultCode, String.valueOf(resultCode));
	}
	
	public String getResultDescription() {
		return resultDescription;
	}

	public void setResultDescription(String resultDescription) {
		this.resultDescription = resultDescription;
	}

	public Face getFace() {
		return face;
	}

	public void setFace(Face face) {
		this.face = face;
	}
	
	@SuppressWarnings("serial")
	Map<Integer, String> errors = new HashMap<Integer, String>() {{
		put(0, "OK"); // FSDKE_OK
		put(-1, "Falha"); // FSDKE_FAILED
		put(-2, "SDK n�oo ativada"); // FSDKE_NOT_ACTIVATED
		put(-3, "Falta de mem�ria"); // FSDKE_OUT_OF_MEMORY
		put(-4, "Argumentos inv�lidos"); // FSDKE_INVALID_ARGUMENT
		put(-5, "Erro de entrada de dados"); // FSDKE_IO_ERROR
		put(-6, "Imagem muito pequena"); // FSDKE_IMAGE_TOO_SMALL
		put(-7, "Rosto n�o encontrado"); // FSDKE_FACE_NOT_FOUND
		put(-8, "Tamanho de buffer insuficiente"); // FSDKE_INSUFFICIENT_BUFFER_SIZE
		put(-9, "Extens�o de arquivo n�o suportado"); // FSDKE_UNSUPPORTED_IMAGE_EXTENSION
		put(-10, "N�o foi poss�vel abrir o arquivo"); // FSDKE_CANNOT_OPEN_FILE
		put(-11, "N�o foi poss�vel criar o arquivo"); // FSDKE_CANNOT_CREATE_FILE
		put(-12, "Formato de arquivo inv�lido"); // FSDKE_BAD_FILE_FORMAT
		put(-13, "Arquivo n�o encontrado"); // FSDKE_FILE_NOT_FOUND
		put(-14, "Conex�o fechada"); // FSDKE_CONNECTION_CLOSED
		put(-15, "Conex�o falhou"); // FSDKE_CONNECTION_FAILED
		put(-16, "Inicializa��o IP falhou"); // FSDKE_IP_INIT_FAILED
		put(-17, "Necessita ativa��o do servidor"); // FSDKE_NEED_SERVER_ACTIVATION
		put(-18, "ID n�o encontrado"); // FSDKE_ID_NOT_FOUND
		put(-19, "Atributo n�o detectado"); // FSDKE_ATTRIBUTE_NOT_DETECTED
		put(-20, "Limite de mem�ria do tracker insuficiente"); // FSDKE_INSUFFICIENT_TRACKER_MEMORY_LIMIT
		put(-21, "Atributo desconhecido"); // FSDKE_UNKNOWN_ATTRIBUTE
		put(-22, "Vers�o de arquivo n�o suportada"); // FSDKE_UNSUPPORTED_FILE_VERSION
		put(-23, "Erro de sintaxe"); // FSDKE_SYNTAX_ERROR
		put(-24, "Par�metro n�o encontrado"); // FSDKE_PARAMETER_NOT_FOUND
		put(-25, "Template inv�lido"); // FSDKE_INVALID_TEMPLATE
		put(-26, "Vers�o do template n�o suportada"); // FSDKE_UNSUPPORTED_TEMPLATE_VERSION
		put(-27, "�ndice da c�mera n�o existe"); // FSDKE_CAMERA_INDEX_DOES_NOT_EXIST
		put(-28, "Plataforma n�o licensiada"); // FSDKE_PLATFORM_NOT_LICENSED
		put(-29, "Muitos rostos na imagem"); // FSDKE_TOO_MANY_FACES_ON_IMAGE
		put(-30, "Exce��o no servidor"); // FSDKE_SERVER_EXCEPTION
		put(-31, "Usu�rio j� registrado"); // FSDKE_USER_ALREADY_REGISTERED
		put(-32, "Usu�rio registrado com outro nome"); // FSDKE_USER_REGISTERED_WITH_OTHER_NAME
		put(-33, "Reconhecimento falhou"); // FSDKE_RECOGNITION_FAILED
		put(-34, "Erro ao converter imagem para buffer"); // FSDKE_IMAGE_TO_BUFFER_ERROR
		put(-35, "Usu�rio n�o encontrado"); // FSDKE_USER_NOT_FOUND
		put(-36, "Usu�rio n�o est� logado no servidor"); // FSDKE_USER_NOT_LOGGED_IN
		put(-37, "Exce��o local"); // FSDKE_LOCAL_EXCEPTION
	}};

}
