package com.protreino.services.screens;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.text.JTextComponent;
import javax.swing.text.MaskFormatter;

import org.apache.commons.lang.StringUtils;
import org.apache.derby.iapi.sql.compile.Visitable;

import com.protreino.services.devices.Device;
import com.protreino.services.devices.FacialDevice;
import com.protreino.services.devices.ServerDevice;
import com.protreino.services.entity.CargoEntity;
import com.protreino.services.entity.CentroCustoEntity;
import com.protreino.services.entity.DepartamentoEntity;
import com.protreino.services.entity.EmpresaEntity;
import com.protreino.services.entity.ParametroEntity;
import com.protreino.services.entity.PedestreRegraEntity;
import com.protreino.services.entity.PedestrianAccessEntity;
import com.protreino.services.entity.RegraEntity;
import com.protreino.services.enumeration.TipoPedestre;
import com.protreino.services.enumeration.TipoRegra;
import com.protreino.services.main.Main;
import com.protreino.services.utils.Constants;
import com.protreino.services.utils.CropImage;
import com.protreino.services.utils.HibernateUtil;
import com.protreino.services.utils.SelectItem;
import com.protreino.services.utils.Utils;

@SuppressWarnings("serial")
public class RegisterVisitorDialog extends JDialog {
	private int ICON_SIZE = 158;

	private Font font;
	private Font tabHeaderFont;

	private PedestrianAccessEntity visitante;

	private JDialog escolherFotoDialog;
	private JDialog cropImageDialog;
	
	private JLabel nomeLabel;
	private JLabel dataNascimentoLabel;
	private JLabel emailLabel;
	private JLabel cpfLabel;
	private JLabel generoLabel;
	private JLabel rgLabel;
	private JLabel telefoneLabel;
	private JLabel celularLabel;
	private JLabel responsavelLabel;
	private JLabel obsLabel;
	private JLabel cepLabel;
	private JLabel cartaoAcessoLabel;
	private JLabel empresaLabel;
	private JLabel departamentoLabel;
	private JLabel centroCustoLabel;
	private JLabel cargoLabel;
	private JLabel matriculaLabel;
	
	private JButton openImageSelectButton;
	private JTextField nomeTextField;
	private JFormattedTextField dataNascimentoTextField;
	private JTextField emailTextField;
	private JFormattedTextField cpfTextField;
	private JComboBox<SelectItem> generoJComboBox;
	private JTextField rgTextField;
	private JFormattedTextField telefoneTextField;
	private JFormattedTextField celularTextField;
	private JTextArea obsTextArea;
	private JTextField responsavelTextField;
	private JComboBox<SelectItem> statusJComboBox;
	private JFormattedTextField cartaoAcessoTextField;
	private JTextField matriculaTextField;
	
	private JCheckBox habilitarTecladoCheckBox;
	private JCheckBox enviaSMSdeConfirmacaoEntrada;
	private JCheckBox sempreLiberado;
	
	private JComboBox<SelectItem> empresaJComboBox;
	private JComboBox<SelectItem> departamentoJComboBox;
	private JComboBox<SelectItem> centroCustoJComboBox;
	private JComboBox<SelectItem> cargoJComboBox;

	private JFormattedTextField cepTextField;
	private JTextField logradouroTextField;
	private JTextField numeroTextField;
	private JTextField complementoTextField;
	private JTextField bairroTextField;
	private JTextField cidadeTextField;
	private JTextField estadoTextField;

	private ImageIcon escolherImagemIcon;

	private Container mainContentPane;
	private JTabbedPane tabbedPane;

	private JPanel barraLateralPanel;
	private JPanel actionsPanel;

	private JButton addVisitorButton;
	private JButton cancelarButton;
	private JButton cadastrarDigitalButton;
	private JButton cadastrarFaceButton;
	private JButton addCreditoButton;

	private BufferedImage bufferedImage;

	public JLabel sampleLabel;

	private byte[] fotoVisitante;

	private Integer qtdeDigitosCartao;

	private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

	private AvailableDevicesPanel panelEquipamentosDisponiveis;

	private MensagensPersonalizadasPanel panelMensagemPersonalizadas;

	private AdicionarDocumentoPanel panelAdicionarDocumento;

	private AdicionarRegrasPanel panelAdicionarRegras;

	private JPanel panelInternoLateral;
	
	private boolean habilitaBuscaCPF = false;
	private boolean habilitaBuscaRG  = false;
	public static boolean abertoPeloAtalho = false;
	
	

	public RegisterVisitorDialog(PedestrianAccessEntity visitante) {
		loadImages();

		this.visitante = visitante;

		setIconImage(Main.favicon);
		setModal(true);
		setTitle("PEDESTRE".equals(this.visitante.getTipo()) ? "Cadastrar Pedestre" : "Cadastrar Visitante");
		setResizable(false);
		setLayout(new BorderLayout());
		
		verificaRegrasBusca();

		font = getDefaultFont();
		Font font2 = font;
		tabHeaderFont = new Font(font2.getFontName(), Font.BOLD, font2.getSize() + 1);

		mainContentPane = new Container();
		mainContentPane.setLayout(new BorderLayout());

		tabbedPane = new JTabbedPane();
		
		JPanel dadosBasicosPanel = montarPanelDadosBasicos();
		tabbedPane.add("Dados b�sicos", dadosBasicosPanel);
		JLabel label = new JLabel("Dados b�sicos");
		label.setPreferredSize(new Dimension(120, 25));
		label.setForeground(Main.firstColor);
		label.setFont(tabHeaderFont);
		tabbedPane.setTabComponentAt(0, label);

		JPanel enderecoPanel = montarPanelEndereco();
		tabbedPane.add("Endere�o", enderecoPanel);
		label = new JLabel("Endere�o");
		label.setPreferredSize(new Dimension(100, 25));
		label.setForeground(Main.firstColor);
		label.setFont(tabHeaderFont);
		tabbedPane.setTabComponentAt(1, label);
		
		JPanel regrasPanel = montaPainelAdicionarRegra();
		tabbedPane.add("Regras de acesso", regrasPanel);
		label = new JLabel("Regras de acesso");
		label.setPreferredSize(new Dimension(150, 25));
		label.setForeground(Main.firstColor);
		label.setFont(tabHeaderFont);
		tabbedPane.setTabComponentAt(2, label);
		
		JPanel equipamentosPanel = montaPainelEquipamentos();
		tabbedPane.add("Equipamentos", equipamentosPanel);
		label = new JLabel("Equipamentos");
		label.setPreferredSize(new Dimension(130, 25));
		label.setForeground(Main.firstColor);
		label.setFont(tabHeaderFont);
		tabbedPane.setTabComponentAt(3, label);
		
		JPanel documentosPanel = montaPainelAdicionarDocumento();
		tabbedPane.add("Documentos", documentosPanel);
		label = new JLabel("Documentos");
		label.setPreferredSize(new Dimension(100, 25));
		label.setForeground(Main.firstColor);
		label.setFont(tabHeaderFont);
		tabbedPane.setTabComponentAt(4, label);
		
		JPanel mensagensPanel = montaPainelMensagemPersonalizadas();
		tabbedPane.add("Mensagens", mensagensPanel);
		label = new JLabel("Mensagens");
		label.setPreferredSize(new Dimension(100, 25));
		label.setForeground(Main.firstColor);
		label.setFont(tabHeaderFont);
		tabbedPane.setTabComponentAt(5, label);

		actionsPanel = montarPainelAcoes();
		barraLateralPanel = montarPainelLateral();

		if (this.visitante != null && this.visitante.getId() != null) {
			preencheDadosVisitanteEditando();
		}

		mainContentPane.add(tabbedPane, BorderLayout.CENTER);
		mainContentPane.add(actionsPanel, BorderLayout.SOUTH);
		mainContentPane.add(barraLateralPanel, BorderLayout.WEST);
		
		addWindowListener(new WindowAdapter() {
		    @Override
		    public void windowClosing(WindowEvent e) {
		       abertoPeloAtalho = false;
		    }
		});

		getContentPane().add(mainContentPane, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private void verificaRegrasBusca() {
		habilitaBuscaCPF = false;
		habilitaBuscaRG  = false;
		
		String camposObrigatorios = buscaParametroPeloNome("Campos obrigat�rios para cadastro de pedestres");
		if(camposObrigatorios.contains("cpf")) 
			habilitaBuscaCPF = true;
		
		if(camposObrigatorios.contains("rg"))
			habilitaBuscaRG = true;
	}

	private JPanel montarPanelDadosBasicos() {
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;

		nomeLabel = getNewLabel("Nome");
		nomeTextField = getNewTextField(30);
		nomeTextField.setMinimumSize(new Dimension(300, 25));
		JPanel nomePanel = getNewMiniPanel(nomeLabel, nomeTextField);
		panel.add(nomePanel, getNewGridBag(0, 0, 30, 5));
		
		dataNascimentoLabel = getNewLabel("Data de nascimento");
		dataNascimentoTextField = Utils.getNewJFormattedTextField(10);
		MaskFormatter mask = Utils.getNewMaskFormatter("##/##/####");
		mask.install(dataNascimentoTextField);
		JPanel dataNascimentoPanel = getNewMiniPanel(dataNascimentoLabel, dataNascimentoTextField);
		panel.add(dataNascimentoPanel, getNewGridBag(1, 0, 30, 5));

		emailLabel = getNewLabel("E-mail");
		emailTextField = getNewTextField(25);
		emailTextField.setMinimumSize(new Dimension(300, 25));
		JPanel emailPanel = getNewMiniPanel(emailLabel, emailTextField);
		panel.add(emailPanel, getNewGridBag(2, 0, 30, 5));

		cpfLabel = getNewLabel("CPF");
		cpfTextField = Utils.getNewJFormattedTextField(25);
		cpfTextField.setMinimumSize(new Dimension(300, 25));
		mask = Utils.getNewMaskFormatter("###.###.###-##");
		mask.install(cpfTextField);
		JPanel cpfPanel = getNewMiniPanel(cpfLabel, cpfTextField);
		panel.add(cpfPanel, getNewGridBag(0, 1, 30, 5));
		cpfTextField.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				//pesquisa por pedestre ou visitante
				if(habilitaBuscaCPF && visitante.getId() == null && cpfTextField.getText() != null 
						&& !"".equals(cpfTextField.getText())
						&& !"".equals(cpfTextField.getText().replace(".", "").replace("-", ""))) {
					PedestrianAccessEntity existente = (PedestrianAccessEntity) HibernateUtil
							.getSingleResultByCPF(PedestrianAccessEntity.class, cpfTextField.getText().replace(".", "").replace("-", "").trim());
					
					if(existente != null && visitante.getTipo().equals(existente.getTipo())
							&& !"".equals(existente.getCpf())
							&& !"".equals(existente.getCpf().replace(".", "").replace("-", ""))) {
						visitante = existente;
						fotoVisitante = visitante.getFoto();
						preencheDadosVisitanteEditando();
						ajustaPaineisParaEdicao();
						buscaESelecionaEmpresaPedestre();
						
						mainContentPane.remove(actionsPanel);
						actionsPanel = montarPainelAcoes();
						mainContentPane.add(actionsPanel, BorderLayout.SOUTH);
						mainContentPane.revalidate();
						mainContentPane.repaint();
					}
				}
			}
			@Override
			public void focusGained(FocusEvent e) {
			}
		});

		Vector<SelectItem> itens = new Vector<SelectItem>();
		itens.add(new SelectItem("MASCULINO", "MASCULINO"));
		itens.add(new SelectItem("FEMININO", "FEMININO"));

		generoLabel = getNewLabel("G�nero");
		generoJComboBox = new JComboBox<SelectItem>(itens);
		JPanel generoPanel = new JPanel();

		generoPanel.setLayout(new GridBagLayout());
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		generoPanel.add(generoLabel, c);
		c.gridx = 0;
		c.gridy = 1;
		generoPanel.add(generoJComboBox, c);
		panel.add(generoPanel, getNewGridBag(1, 1, 30, 5));

		rgLabel = getNewLabel("RG");
		rgTextField = getNewTextField(25);
		rgTextField.setMinimumSize(new Dimension(300, 25));
		JPanel rgPanel = getNewMiniPanel(rgLabel, rgTextField);
		panel.add(rgPanel, getNewGridBag(2, 1, 30, 5));
		rgTextField.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				//pesquisa por pedestre ou visitante
				if(habilitaBuscaRG && visitante.getId() == null && rgTextField.getText() != null 
						&& !"".equals(rgTextField.getText())) {
					PedestrianAccessEntity existente = (PedestrianAccessEntity) HibernateUtil
							.getSingleResultByRG(PedestrianAccessEntity.class, rgTextField.getText().trim());
					if(existente != null && visitante.getTipo().equals(existente.getTipo())
							&& !"".equals(existente.getRg())) {
						visitante = existente;
						fotoVisitante = visitante.getFoto();
						preencheDadosVisitanteEditando();
						ajustaPaineisParaEdicao();
						buscaESelecionaEmpresaPedestre();
						
						mainContentPane.remove(actionsPanel);
						actionsPanel = montarPainelAcoes();
						mainContentPane.add(actionsPanel, BorderLayout.SOUTH);
						mainContentPane.revalidate();
						mainContentPane.repaint();
					}
				}
			}
			@Override
			public void focusGained(FocusEvent e) {
			}
		});

		telefoneLabel = getNewLabel("Telefone");
		telefoneTextField = Utils.getNewJFormattedTextField(25);
		telefoneTextField.setMinimumSize(new Dimension(300, 25));
		mask = Utils.getNewMaskFormatter("(###) ####-####");
		mask.install(telefoneTextField);
		JPanel telefonePanel = getNewMiniPanel(telefoneLabel, telefoneTextField);
		panel.add(telefonePanel, getNewGridBag(0, 2, 30, 5));

		celularLabel = getNewLabel("Celular");
		celularTextField = Utils.getNewJFormattedTextField(18);
		mask = Utils.getNewMaskFormatter("(###) # ####-####");
		mask.install(celularTextField);
		JPanel celularPanel = getNewMiniPanel(celularLabel, celularTextField);
		panel.add(celularPanel, getNewGridBag(1, 2, 30, 5));

		responsavelLabel = getNewLabel("Respons�vel");
		responsavelTextField = getNewTextField(25);
		responsavelTextField.setMinimumSize(new Dimension(300, 25));
		JPanel responsavelPanel = getNewMiniPanel(responsavelLabel, responsavelTextField);
		panel.add(responsavelPanel, getNewGridBag(2, 2, 30, 5));

		obsLabel = getNewLabel("Observa��es");
		obsTextArea = new JTextArea();
		obsTextArea.setColumns(40);
		obsTextArea.setRows(4);
		obsTextArea.setLineWrap(true);
		obsTextArea.getInputMap().put(KeyStroke.getKeyStroke("control V"), "none");
		obsTextArea.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				if (obsTextArea.getText().length() > 150) {
					e.consume();
				}
			}
		});
		obsTextArea.setMinimumSize(new Dimension(300,70));

		JPanel obsPanel = getNewMiniPanel(obsLabel, obsTextArea);
		panel.add(obsPanel, getNewGridBag(0, 3, 30, 5));

		criaPanelDadosEmpresa(panel);

		return panel;
	}
	
	private JPanel montaPainelEquipamentos() {
		panelEquipamentosDisponiveis = new AvailableDevicesPanel();

		return panelEquipamentosDisponiveis;
	}
	
	private JPanel montaPainelMensagemPersonalizadas() {
		panelMensagemPersonalizadas = new MensagensPersonalizadasPanel();
		return panelMensagemPersonalizadas;
	}
	
	private JPanel montaPainelAdicionarDocumento() {
		panelAdicionarDocumento = new AdicionarDocumentoPanel();
		return panelAdicionarDocumento;
	}
	
	private JPanel montaPainelAdicionarRegra() {
		panelAdicionarRegras = new AdicionarRegrasPanel(TipoPedestre.valueOf(visitante.getTipo()));
		return panelAdicionarRegras;
	}

	private void criaPanelDadosEmpresa(JPanel panel) {
		empresaLabel = new JLabel("Empresa");
		empresaJComboBox = new JComboBox<SelectItem>(getAllEmpresasSelectItens());
		criaPainelComboBox(empresaLabel, empresaJComboBox, panel, 0, 4);
		
		if(visitante.getIdEmpresa() != null) {
			buscaESelecionaEmpresaPedestre();
		}
		
		empresaJComboBox.addActionListener(event -> {
			SelectItem selectedItem = (SelectItem) empresaJComboBox.getSelectedItem();
			
			Long idEmpresaSelecionada = (Long) selectedItem.getValue();
			
			departamentoJComboBox = new JComboBox<SelectItem>(getAllDepartamentosSelectItens(idEmpresaSelecionada));
			centroCustoJComboBox = new JComboBox<SelectItem>(getAllCentroCustosSelectItens(idEmpresaSelecionada));
			cargoJComboBox = new JComboBox<SelectItem>(getAllCargosSelectItens(idEmpresaSelecionada));
			
			//Remove os tres ultimos componentes
			panel.remove(panel.getComponent(panel.getComponentCount() - 1));
			panel.remove(panel.getComponent(panel.getComponentCount() - 1));
			panel.remove(panel.getComponent(panel.getComponentCount() - 1));
			
			//Adiciona novamente os componentes com os valores combobox atualizados
			criaPainelComboBox(departamentoLabel, departamentoJComboBox, panel, 1, 4);
			criaPainelComboBox(centroCustoLabel, centroCustoJComboBox, panel, 0, 5);
			criaPainelComboBox(cargoLabel, cargoJComboBox, panel, 1, 5);
			
			panel.updateUI();
			panel.repaint();
		});
		
		departamentoLabel = new JLabel("Departamento");
		if(departamentoJComboBox == null)
			departamentoJComboBox = new JComboBox<SelectItem>();
		
		criaPainelComboBox(departamentoLabel, departamentoJComboBox, panel, 1, 4);
		
		centroCustoLabel = new JLabel("Centro de Custo");
		if(centroCustoJComboBox == null)
			centroCustoJComboBox = new JComboBox<SelectItem>();
		criaPainelComboBox(centroCustoLabel, centroCustoJComboBox, panel, 0, 5);
		
		cargoLabel = new JLabel("Cargo");
		if(cargoJComboBox == null)
			cargoJComboBox = new JComboBox<SelectItem>();
		criaPainelComboBox(cargoLabel, cargoJComboBox, panel, 1, 5);
	}

	private void buscaESelecionaEmpresaPedestre() {
		int sizeEmp = empresaJComboBox.getItemCount();
		
		for(int i = 0; i < sizeEmp; i++) {
			SelectItem item = empresaJComboBox.getItemAt(i);
			
			if(visitante.getIdEmpresa() == null)
				continue;
			
			if(!visitante.getIdEmpresa().equals(item.getValue()))
				continue;
			
			empresaJComboBox.setSelectedIndex(i);
			
			departamentoJComboBox = new JComboBox<SelectItem>(getAllDepartamentosSelectItens(Long.valueOf(visitante.getIdEmpresa())));
			centroCustoJComboBox = new JComboBox<SelectItem>(getAllCentroCustosSelectItens(Long.valueOf(visitante.getIdEmpresa())));
			cargoJComboBox = new JComboBox<SelectItem>(getAllCargosSelectItens(Long.valueOf(visitante.getIdEmpresa())));
			
			if(visitante.getIdDepartamento() != null) {
				int sizeDept = departamentoJComboBox.getItemCount();
				
				for(int j = 0; j < sizeDept; j++) {
					SelectItem dept = departamentoJComboBox.getItemAt(j);
					
					if(visitante.getIdDepartamento().equals(dept.getValue())) {
						departamentoJComboBox.setSelectedIndex(j);
					}
				}
			}
			
			if(visitante.getIdCentroCusto() != null) {
				int sizeCC = centroCustoJComboBox.getItemCount();
				
				for(int j = 0; j < sizeCC; j++) {
					SelectItem cc = centroCustoJComboBox.getItemAt(j);
					
					if(visitante.getIdCentroCusto().equals(cc.getValue())) {
						centroCustoJComboBox.setSelectedIndex(j);
					}
				}
			}
			
			if(visitante.getIdCargo() != null) {
				int sizeCargo = cargoJComboBox.getItemCount();
				
				for(int j = 0; j < sizeCargo; j++) {
					SelectItem cargo = cargoJComboBox.getItemAt(j);
					
					if(visitante.getIdCargo().equals(cargo.getValue())) {
						cargoJComboBox.setSelectedIndex(j);
					}
				}
			}
		}
	}
	
	private JPanel montarPanelEndereco() {
		JPanel panelExterno = new JPanel();
		panelExterno.setLayout(new BorderLayout());

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		panel.setLayout(new GridBagLayout());
		
		cepLabel = getNewLabel("CEP");
		cepTextField = Utils.getNewJFormattedTextField(10);
		MaskFormatter mask = Utils.getNewMaskFormatter("#####-###");
		mask.install(cepTextField);
		JPanel cepPanel = getNewMiniPanel(cepLabel, cepTextField);
		panel.add(cepPanel, getNewGridBag(0, 0, 30, 5));

		JLabel logradouroLabel = getNewLabel("Logradouro");
		logradouroTextField = getNewTextField(20);
		JPanel logradouroPanel = getNewMiniPanel(logradouroLabel, logradouroTextField);
		panel.add(logradouroPanel, getNewGridBag(1, 0, 30, 5));

		JLabel numeroLabel = getNewLabel("N�mero");
		numeroTextField = getNewTextField(10);
		JPanel numeroPanel = getNewMiniPanel(numeroLabel, numeroTextField);
		panel.add(numeroPanel, getNewGridBag(2, 0, 30, 5));

		JLabel complementoLabel = getNewLabel("Complemento");
		complementoTextField = getNewTextField(20);
		JPanel complementoPanel = getNewMiniPanel(complementoLabel, complementoTextField);
		panel.add(complementoPanel, getNewGridBag(0, 1, 30, 5));

		JLabel bairroLabel = getNewLabel("Bairro");
		bairroTextField = getNewTextField(20);
		JPanel bairroPanel = getNewMiniPanel(bairroLabel, bairroTextField);
		panel.add(bairroPanel, getNewGridBag(1, 1, 30, 5));

		JLabel cidadeLabel = getNewLabel("Cidade");
		cidadeTextField = getNewTextField(27);
		JPanel cidadePanel = getNewMiniPanel(cidadeLabel, cidadeTextField);
		panel.add(cidadePanel, getNewGridBag(2, 1, 30, 5));

		JLabel estadoLabel = getNewLabel("Estado");
		estadoTextField = getNewTextField(10);
		JPanel estadoPanel = getNewMiniPanel(estadoLabel, estadoTextField);
		panel.add(estadoPanel, getNewGridBag(0, 2, 30, 5));

		panelExterno.add(panel, BorderLayout.NORTH);
		return panelExterno;
	}
	
	public void ajustaPaineisParaEdicao() {
		
		if (this.fotoVisitante != null) {
			openImageSelectButton.setIcon(new ImageIcon(createMiniImage(fotoVisitante)));
		} else {
			openImageSelectButton.setIcon(escolherImagemIcon);
		}
		
		
	}

	private JPanel montarPainelLateral() {
		JPanel panelExterno = new JPanel(new BorderLayout());
		
		panelInternoLateral = new JPanel();
		panelInternoLateral.setBorder(new EmptyBorder(0, 10, 0, 10));
		panelInternoLateral.setLayout(new GridBagLayout());

		if (this.visitante != null && this.visitante.getFoto() != null) {
			this.fotoVisitante = this.visitante.getFoto();
		}

		openImageSelectButton = new JButton();

		if (this.fotoVisitante != null) {
			openImageSelectButton.setIcon(new ImageIcon(createMiniImage(fotoVisitante)));
		} else {
			openImageSelectButton.setIcon(escolherImagemIcon);
		}
		openImageSelectButton.setPreferredSize(new Dimension(ICON_SIZE, ICON_SIZE));
		openImageSelectButton.setMaximumSize(new Dimension(ICON_SIZE, ICON_SIZE));
		openImageSelectButton.setToolTipText("Escolher imagem");

		openImageSelectButton.addActionListener(e -> {
			criarDialogoEscolherFoto();
		});

		GridBagConstraints c = getNewGridBag(0, 0, 20, 5);
		panelInternoLateral.add(openImageSelectButton, c);

		JPanel statusPanel = new JPanel();
		statusPanel.setLayout(new GridBagLayout());
		Vector<SelectItem> itens = new Vector<SelectItem>();
		itens.add(new SelectItem("ATIVO", "ATIVO"));
		itens.add(new SelectItem("INATIVO", "INATIVO"));
		JLabel statusLabel = new JLabel("Status");
		c = getNewGridBag(0, 0, 0, 0);
		statusPanel.add(statusLabel, c);
		statusJComboBox = new JComboBox<SelectItem>(itens);
		statusJComboBox.setPreferredSize(new Dimension(100, 25));
		statusJComboBox.setMaximumSize(new Dimension(100, 25));
		c = getNewGridBag(0, 1, 0, 0);
		statusPanel.add(statusJComboBox, c);
		panelInternoLateral.add(statusPanel, getNewGridBag(0, 1, 30, 5));

		if (Main.loggedUser != null && Main.loggedUser.getQtdePadraoDigitosCartao() != null) {
			qtdeDigitosCartao = Main.loggedUser.getQtdePadraoDigitosCartao();
		} else {
			qtdeDigitosCartao = 5;
		}

		JPanel cartaoAcessoPanel = new JPanel(new GridBagLayout());
		cartaoAcessoLabel = new JLabel("Cart�o de acesso");
		c = getNewGridBag(0, 0, 0, 0);
		cartaoAcessoPanel.add(cartaoAcessoLabel, c);
		cartaoAcessoTextField = Utils.getNewJFormattedTextField(15);
		cartaoAcessoTextField.setText(StringUtils.leftPad(cartaoAcessoTextField.getText(), qtdeDigitosCartao, '0'));
		cartaoAcessoTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				completaComZeros(e);
			}
		});
		c = getNewGridBag(0, 1, 0, 0);
		cartaoAcessoPanel.add(cartaoAcessoTextField, c);
		panelInternoLateral.add(cartaoAcessoPanel, getNewGridBag(0, 2, 30, 5));
		
		JPanel matriculaPanel = new JPanel(new GridBagLayout());
		matriculaPanel.setVisible(isExibeCampoMatricula());
		matriculaLabel = new JLabel("Matr�cula");
		c = getNewGridBag(0, 0, 0, 0);
		matriculaPanel.add(matriculaLabel, c);
		matriculaTextField = getNewTextField(15);
		matriculaTextField.setEnabled(isHabilitaCampoMatricula());
		c = getNewGridBag(0, 1, 0, 0);
		matriculaPanel.add(matriculaTextField, c);
		panelInternoLateral.add(matriculaPanel, getNewGridBag(0, 3, 30, 5));

		habilitarTecladoCheckBox = new JCheckBox("Habilitar teclado");
//		habilitarTecladoCheckBox.addItemListener(e -> {
//			visitante.setHabilitarTeclado(e.getStateChange() == ItemEvent.SELECTED);
//		});
		panelInternoLateral.add(habilitarTecladoCheckBox, getNewGridBag(0, 4, 20, 5));
		
		enviaSMSdeConfirmacaoEntrada = new JCheckBox("Enviar SMS ao entrar");
		enviaSMSdeConfirmacaoEntrada.setVisible(Main.loggedUser.getChaveIntegracaoComtele() != null
												&& !Main.loggedUser.getChaveIntegracaoComtele().isEmpty());
//		enviaSMSdeConfirmacaoEntrada.addItemListener(e -> {
//			visitante.setEnviaSmsAoPassarNaCatraca(e.getStateChange() == ItemEvent.SELECTED);
//		});
		panelInternoLateral.add(enviaSMSdeConfirmacaoEntrada, getNewGridBag(0, 5, 20, 5));
		
		sempreLiberado = new JCheckBox("Sempre liberado");
		sempreLiberado.setVisible(exibeBotaoSempreLiberado());
//		sempreLiberado.addItemListener(e -> {
//			visitante.setSempreLiberado(e.getStateChange() == ItemEvent.SELECTED);
//		});
		panelInternoLateral.add(sempreLiberado, getNewGridBag(0, 6, 20, 5));
		
		panelExterno.add(panelInternoLateral, BorderLayout.NORTH);
		
		return panelExterno;
	}

	private boolean exibeBotaoSempreLiberado() {
		String exibeBotaoSempreLiberadoParaTodos = buscaParametroPeloNome("Exibe escolha \"Sempre liberado\" para todos os usu�rios");
		
		return Boolean.TRUE.equals(Boolean.valueOf(exibeBotaoSempreLiberadoParaTodos))
						|| "PEDESTRE".equals(visitante.getTipo());
	}

	private JPanel montarPainelAcoes() {
		addCreditoButton = new JButton("Salvar e liberar um acesso");
		addCreditoButton.setBorder(new EmptyBorder(20, 20, 20, 20));
		addCreditoButton.setPreferredSize(new Dimension(150, 40));
		addCreditoButton.addActionListener(e -> {
			criarDialogoConfirmarAddCredito();
		});
		addCreditoButton.setVisible(isBotaoAddCreditoVisivel());

		cadastrarDigitalButton = new JButton("Cadastrar digital");
		cadastrarDigitalButton.setBorder(new EmptyBorder(20, 20, 20, 20));
		cadastrarDigitalButton.setPreferredSize(new Dimension(150, 40));
		cadastrarDigitalButton.addActionListener(e -> {
			criarDialogoEscolherCatracaParaCadastroDigital();
		});

		cadastrarFaceButton = new JButton("Cadastrar face");
		cadastrarFaceButton.setBorder(new EmptyBorder(20, 20, 20, 20));
		cadastrarFaceButton.setPreferredSize(new Dimension(150, 40));
		cadastrarFaceButton.addActionListener(e -> {
			if (Boolean.TRUE.equals(this.visitante.getCadastradoNoDesktop()))
				criarDialogoUsuarioNaoPermitidoCadastrarFace();
			else
				criarDialogoEscolherCameraParaCadastroFace();
		});

		addVisitorButton = new JButton("Salvar");
		addVisitorButton.setBorder(new EmptyBorder(20, 20, 20, 20));
		addVisitorButton.setPreferredSize(new Dimension(150, 40));
		addVisitorButton.addActionListener(e -> {
			boolean valido = validarCampos();
			if (valido) {
				adicionarVisitante();
				limparTodosOsCampos();
				this.dispose();
				
				if(Utils.getPreferenceAsBoolean("pedestrianAlwaysOpen")) {
					new Thread() {
						public void run() {
							Utils.sleep(500);
							if("VISITANTE".equals(visitante.getTipo()))
								Main.mainScreen.abreCadastroVisitante();
							else
								Main.mainScreen.abreCadastroPedestre();
						}
					}.start();
				}else {
					Main.mainScreen.refreshAll();
				}
			}
		});

		cancelarButton = new JButton("Cancelar");
		cancelarButton.setBorder(new EmptyBorder(20, 15, 20, 15));
		cancelarButton.setPreferredSize(new Dimension(130, 40));
		cancelarButton.addActionListener(e -> {
			this.dispose();
		});

		JPanel panelInterno = new JPanel();
		panelInterno.setLayout(new BoxLayout(panelInterno, BoxLayout.X_AXIS));
		
		if (possuiCatracaDisponivel() && isEditandoVisitante()) {
			panelInterno.add(cadastrarDigitalButton);
			panelInterno.add(Box.createHorizontalStrut(10));
		}
		if (possuiCameraDisponivel() && isEditandoVisitante()) {
			panelInterno.add(cadastrarFaceButton);
			panelInterno.add(Box.createHorizontalStrut(10));
		}
		
		panelInterno.add(addCreditoButton);
		panelInterno.add(Box.createHorizontalStrut(10));
		panelInterno.add(addVisitorButton);
		panelInterno.add(Box.createHorizontalStrut(10));
		panelInterno.add(cancelarButton);

		JPanel panel = new JPanel();

		if (!isEditandoVisitante())
			panel.setBorder(new EmptyBorder(5, 0, 10, -50));
		else if (!possuiCameraDisponivel() && !possuiCatracaDisponivel() && isEditandoVisitante())
			panel.setBorder(new EmptyBorder(5, 0, 10, -50));
		else if (possuiCameraDisponivel() && possuiCatracaDisponivel() && isEditandoVisitante())
			panel.setBorder(new EmptyBorder(5, 0, 10, -100));
		else if (possuiCatracaDisponivel() && isEditandoVisitante())
			panel.setBorder(new EmptyBorder(5, 0, 10, -70));
		else if (possuiCameraDisponivel() && isEditandoVisitante())
			panel.setBorder(new EmptyBorder(5, 0, 10, -120));

		panel.setLayout(new BorderLayout());
		panel.add(panelInterno, BorderLayout.EAST);

		return panel;
	}

	private void adicionarVisitante() {
		String regex = ".*\\d.*";

		if (visitante.getId() == null) {
			visitante.setId(new Date().getTime());
			visitante.setIdTemp(visitante.getId());
			visitante.setCadastradoNoDesktop(true);
			visitante.setIdUsuario(Main.loggedUser.getId());
			
		} else {
			
			//verifica se j� existe um visitante novo
			if(visitante.getIdTemp() == null) {
				PedestrianAccessEntity cadastrado = (PedestrianAccessEntity) 
						HibernateUtil.getSingleResultByIdTemp(PedestrianAccessEntity.class, visitante.getId());
				if(cadastrado != null && cadastrado.getId() != null)
					visitante = cadastrado;
			}
			
			visitante.setEditadoNoDesktop(true);
		}

		//Dados basicos
		visitante.setName(nomeTextField.getText());
		try {
			visitante.setDataNascimento(sdf.parse(dataNascimentoTextField.getText()));
		} catch (Exception e) {
			visitante.setDataNascimento(null);
		}
		visitante.setEmail(emailTextField.getText());
		visitante.setCpf(cpfTextField.getText().matches(regex) ? cpfTextField.getText().replace(".", "").replace("-", "") : null);
		visitante.setGenero(generoJComboBox.getSelectedItem() != null ? generoJComboBox.getSelectedItem().toString() : "");
		visitante.setRg(rgTextField.getText().matches(regex) ? rgTextField.getText() : "");
		visitante.setTelefone(telefoneTextField.getText().matches(regex) ? telefoneTextField.getText() : "");
		visitante.setCelular(celularTextField.getText().matches(regex) ? celularTextField.getText() : "");
		visitante.setResponsavel(responsavelTextField.getText());
		visitante.setObservacoes(obsTextArea.getText());
		
		//Aba Lateral
		visitante.setFoto(fotoVisitante);
		visitante.setStatus(statusJComboBox.getSelectedItem().toString());
		visitante.setMatricula(matriculaTextField != null ? matriculaTextField.getText() : null);
		try {
			visitante.setCardNumber(Long.valueOf(cartaoAcessoTextField.getText().replaceAll("[^\\d]", "")).toString());
		}catch (Exception e) {
			visitante.setCardNumber(cartaoAcessoTextField.getText());
		}
		visitante.setEnviaSmsAoPassarNaCatraca(enviaSMSdeConfirmacaoEntrada.isSelected());
		visitante.setSempreLiberado(sempreLiberado.isSelected());
		visitante.setHabilitarTeclado(habilitarTecladoCheckBox.isSelected());

		//Dados Endereco
		visitante.setCep(cepTextField.getText().matches(regex) ? cepTextField.getText() : null);
		visitante.setLogradouro(logradouroTextField.getText());
		visitante.setNumero(numeroTextField.getText());
		visitante.setComplemento(complementoTextField.getText());
		visitante.setBairro(bairroTextField.getText());
		visitante.setCidade(cidadeTextField.getText());
		visitante.setEstado(estadoTextField.getText());

		//Dados Empresa
		visitante.setIdEmpresa(getValorSelecionado(empresaJComboBox));
		visitante.setIdDepartamento(getValorSelecionado(departamentoJComboBox));
		visitante.setIdCentroCusto(getValorSelecionado(centroCustoJComboBox));
		visitante.setIdCargo(getValorSelecionado(cargoJComboBox));

		// verifica cartao zerado
		if (visitante.getCardNumber() != null) {
			Long card = Long.valueOf(visitante.getCardNumber());
			if (card == 0)
				visitante.setCardNumber(null);
		}
		
		if(panelEquipamentosDisponiveis.getPedestresEquipamentos() != null) {
			panelEquipamentosDisponiveis.getPedestresEquipamentos().forEach(p -> {
				p.setPedestrianAccess(visitante);
			});
		}
		visitante.setEquipamentos(panelEquipamentosDisponiveis.getPedestresEquipamentos());
		
		if(panelMensagemPersonalizadas.getMensagens() != null) {
			panelMensagemPersonalizadas.getMensagens().forEach(m -> {
				m.setPedestrianAccess(visitante);
			});
		}
		visitante.setMensagens(panelMensagemPersonalizadas.getMensagens());
		
		if(panelAdicionarDocumento.getDocumentos() != null) {
			panelAdicionarDocumento.getDocumentos().forEach(d -> {
				d.setPedestrianAccess(visitante);
			});
		}
		visitante.setDocumentos(panelAdicionarDocumento.getDocumentos());
		
		if(panelAdicionarRegras.getPedestresRegras() != null
				&& !panelAdicionarRegras.getPedestresRegras().isEmpty()) {
			panelAdicionarRegras.getPedestresRegras().forEach(pr -> {
				pr.setPedestrianAccess(visitante);
			});
			
			visitante.setPedestreRegra(panelAdicionarRegras.getPedestresRegras());

		} else if("VISITANTE".equals(visitante.getTipo())
					&& (visitante.getPedestreRegra() == null 
								|| visitante.getPedestreRegra().isEmpty())) {
			PedestreRegraEntity pedestreRegra = buscaPedestreRegraPadraoVisitante();
			pedestreRegra.setPedestrianAccess(visitante);
			visitante.setPedestreRegra(Arrays.asList(pedestreRegra));
			visitante.setQuantidadeCreditos(visitante.getPedestreRegra().get(0).getQtdeDeCreditos());
			visitante.setValidadeCreditos(visitante.getPedestreRegra().get(0).getValidade());
		}
		
		HibernateUtil.save(PedestrianAccessEntity.class, visitante);
	}

	private void preencheDadosVisitanteEditando() {
		//Dados basicos
		nomeTextField.setText(visitante.getName() != null ? visitante.getName() : "");
		dataNascimentoTextField.setText(visitante.getDataNascimento() != null ? sdf.format(visitante.getDataNascimento()) : "");
		emailTextField.setText(visitante.getEmail() != null ? visitante.getEmail() : "");
		cpfTextField.setText(visitante.getCpf() != null ? visitante.getCpf() : "");
		generoJComboBox.setSelectedIndex(visitante.getGenero() != null 
				? (visitante.getGenero().equals("MASCULINO") ? 0 : 1) : 0);
		rgTextField.setText(visitante.getRg() != null ? visitante.getRg() : "");
		telefoneTextField.setText(visitante.getTelefone() != null ? visitante.getTelefone() : "");
		celularTextField.setText(visitante.getCelular() != null ? visitante.getCelular() : "");
		responsavelTextField.setText(visitante.getResponsavel() != null ? visitante.getResponsavel() : "");
		obsTextArea.setText(visitante.getObservacoes() != null ? visitante.getObservacoes() : "");
		
		statusJComboBox.setSelectedItem(visitante.getStatus() != null ? visitante.getStatus() : null);
		matriculaTextField.setText(visitante.getMatricula() != null ? visitante.getMatricula() : "");
		
		if("VISITANTE".equals(visitante.getTipo()) 
				&& !"".equals(cartaoAcessoTextField.getText().replace("0", ""))
				&& (visitante.getCardNumber() == null  || "".equals(visitante.getCardNumber().replace("0", "")))){
			//n�o muda valor do cart�o adicionado anteriormente
			System.out.println("N�o muda valor do cart�o");
		}else {
		
			cartaoAcessoTextField.setText(visitante.getCardNumber() != null ? visitante.getCardNumber()
														: StringUtils.leftPad(cartaoAcessoTextField.getText(), qtdeDigitosCartao, '0'));
			if (cartaoAcessoTextField.getText().length() < qtdeDigitosCartao)
				cartaoAcessoTextField.setText(StringUtils.leftPad(cartaoAcessoTextField.getText(), qtdeDigitosCartao, '0'));
			
		}
		
		habilitarTecladoCheckBox.setSelected(visitante.getHabilitarTeclado());
		sempreLiberado.setSelected(visitante.getSempreLiberado());
		enviaSMSdeConfirmacaoEntrada.setSelected(visitante.getEnviaSmsAoPassarNaCatraca());
		
		//Endereco
		cepTextField.setText(visitante.getCep() != null ? visitante.getCep() : "");
		logradouroTextField.setText(visitante.getLogradouro() != null ? visitante.getLogradouro() : "");
		numeroTextField.setText(visitante.getNumero() != null ? visitante.getNumero() : "");
		complementoTextField.setText(visitante.getComplemento() != null ? visitante.getComplemento() : "");
		bairroTextField.setText(visitante.getBairro() != null ? visitante.getBairro() : "");
		cidadeTextField.setText(visitante.getCidade() != null ? visitante.getCidade() : "");
		estadoTextField.setText(visitante.getEstado() != null ? visitante.getEstado() : "");

		if(visitante.getEquipamentos() != null)
			panelEquipamentosDisponiveis.setPedestresEquipamentos(visitante.getEquipamentos());
		
		if(visitante.getMensagens() != null)
			panelMensagemPersonalizadas.setMensagens(visitante.getMensagens());
		
		if(visitante.getPedestreRegra() != null)
			panelAdicionarRegras.setPedestresRegras(visitante.getPedestreRegra());
		
		if(visitante.getDocumentos() != null)
			panelAdicionarDocumento.setDocumentos(visitante.getDocumentos());
	}

	private boolean validarCampos() {
		boolean valido = true;
		
		cpfLabel.setText(cpfLabel.getText().replace(" j� existente", ""));
		rgLabel.setText(rgLabel.getText().replace(" j� existente", ""));
		cartaoAcessoLabel.setText(cartaoAcessoLabel.getText().replace(" j� existente", ""));
		matriculaLabel.setText(matriculaLabel.getText().replace(" j� existente", ""));

		restauraFontLabel();

		String camposObrigatorios = buscaParametroPeloNome("Campos obrigat�rios para cadastro de pedestres");
		
		if ("".equals(nomeTextField.getText().trim())) {
			redAndBoldFont(nomeLabel);
			valido = false;
		}
		
		if(camposObrigatorios.contains("data.nascimento")
				&& dataNascimentoTextField.getText()
											.replace("/", "").replace(" ", "")
										.equals("")) {
			redAndBoldFont(dataNascimentoLabel);
			valido = false;
		}
		
		if(camposObrigatorios.contains("email")
				&& emailTextField.getText().equals("")) {
			redAndBoldFont(emailLabel);
			valido = false;
		}
		
		if(camposObrigatorios.contains("cpf")
				&& cpfTextField.getText()
									.replace(".", "").replace("-", "").replace(" ", "")
								.equals("")) {
			redAndBoldFont(cpfLabel);
			valido = false;
		}
		
		if(camposObrigatorios.contains("rg")
				&& rgTextField.getText().equals("")) {
			redAndBoldFont(rgLabel);
			valido = false;
		}
		
		if(camposObrigatorios.contains("telefone")
				&& telefoneTextField.getText()
										.replace("(", "").replace(")", "")
										.replace("-", "").replace(" ", "")
									.equals("")) {
			redAndBoldFont(telefoneLabel);
			valido = false;
		}
		
		if(camposObrigatorios.contains("celular")
				&& celularTextField.getText()
										.replace("(", "").replace(")", "")
										.replace("-", "").replace(" ", "")
									.equals("")) {
			redAndBoldFont(celularLabel);
			valido = false;
		}
		
		if(camposObrigatorios.contains("responsavel")
				&& responsavelTextField.getText().equals("")) {
			redAndBoldFont(responsavelLabel);
			valido = false;
		}
		
		if(camposObrigatorios.contains("observacoes")
				&& obsTextArea.getText().equals("")) {
			redAndBoldFont(obsLabel);
			valido = false;
		}
		
		if(camposObrigatorios.contains("empresa")
				&& empresaJComboBox.getSelectedIndex() < 0) {
			redAndBoldFont(empresaLabel);
			valido = false;
		}
		
		if(camposObrigatorios.contains("departamento")
				&& departamentoJComboBox.getSelectedIndex() < 0) {
			redAndBoldFont(departamentoLabel);
			valido = false;
		}
		
		if(camposObrigatorios.contains("centro.custo")
				&& centroCustoJComboBox.getSelectedIndex() < 0) {
			redAndBoldFont(centroCustoLabel);
			valido = false;
		}
		
		if(camposObrigatorios.contains("cargo")
				&& cargoJComboBox.getSelectedIndex() < 0) {
			redAndBoldFont(cargoLabel);
			valido = false;
		}
		
		if(camposObrigatorios.contains("endereco")
				&& cepTextField.getText()
										.replace("-", "").replace(" ", "")
									.equals("")) {
			redAndBoldFont(cepLabel);
			valido = false;
		}
		
		if(camposObrigatorios.contains("cartao.acesso")
				&& cartaoAcessoTextField.getText().replace("0", "").equals("")) {
			redAndBoldFont(cartaoAcessoLabel);
			valido = false;
		}
		
		if("PEDESTRE".equals(visitante.getTipo())
				&& (panelAdicionarRegras.getPedestresRegras() == null 
						|| panelAdicionarRegras.getPedestresRegras().isEmpty())
					&& !sempreLiberado.isSelected()) {

			criarDialogoPedestreRegraObrigatotio();
			valido = false;
		}
		
		if(valido)
			valido = validaCamposDuplicados();
		
		return valido;
	}
	
	private void criarDialogoPedestreRegraObrigatotio() {
		JDialog regraObrigatoriaDialog = new JDialog();
		regraObrigatoriaDialog.setIconImage(Main.favicon);
		regraObrigatoriaDialog.setModal(true);
		regraObrigatoriaDialog.setTitle("Regra obrigat�ria");
		regraObrigatoriaDialog.setResizable(false);
		regraObrigatoriaDialog.setLayout(new BorderLayout());

		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		JLabel mensagemLabel = new JLabel("Adicione pelo menos uma regra para o pedestre.");
		mensagemLabel.setForeground(Color.red);
		mensagemLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		JButton okButton = new JButton("OK");
		okButton.setBorder(new EmptyBorder(10, 20, 10, 20));
		okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		okButton.addActionListener(e -> {
			regraObrigatoriaDialog.dispose();
		});

		JPanel confirmarPanel = new JPanel();
		confirmarPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
		confirmarPanel.setLayout(new BoxLayout(confirmarPanel, BoxLayout.X_AXIS));
		confirmarPanel.add(okButton);

		mainPanel.add(mensagemLabel);
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add(confirmarPanel);

		regraObrigatoriaDialog.getContentPane().add(mainPanel, BorderLayout.CENTER);
		regraObrigatoriaDialog.pack();
		regraObrigatoriaDialog.setLocationRelativeTo(null);
		regraObrigatoriaDialog.setVisible(true);
	}

	private boolean validaCamposDuplicados() {
		boolean valido = true;
		
		if(cpfTextField != null 
				&& !cpfTextField.getText().replace(".", "").replace("-", "").replace(" ", "").isEmpty()) {
			
			String validaCpfDuplicado = buscaParametroPeloNome("Validar CPF duplicado");
			
			if(Boolean.TRUE.equals(Boolean.valueOf(validaCpfDuplicado))) {
				boolean cpfExiste = verificaCpfExistente(cpfTextField.getText().replace(".", "").replace("-", "").replace(" ", ""),
														visitante.getId() != null ? visitante.getId() : 0l);

				if(cpfExiste) {
					cpfLabel.setText(cpfLabel.getText() + " j� existente");
					redAndBoldFont(cpfLabel);
					valido = false;
				}
			}
		}
		
		if(rgTextField != null
				&& !rgTextField.getText().isEmpty()) {

			String validaRgDuplicado = buscaParametroPeloNome("Validar RG duplicado");
			
			if(Boolean.TRUE.equals(Boolean.valueOf(validaRgDuplicado))) {
				boolean rgExiste = verificaRgExistente(rgTextField.getText(),
													visitante.getId() != null ? visitante.getId() : 0l);
				
				if(rgExiste) {
					rgLabel.setText(rgLabel.getText() + " j� existente");
					redAndBoldFont(rgLabel);
					valido = false;
				}
			}
		}
		
		if(cartaoAcessoTextField != null
				&& !cartaoAcessoTextField.getText().isEmpty()
				&& !cartaoAcessoTextField.getText().replace( "0", "").equals("")) {
			String validaCartaoAcessoDuplicado = buscaParametroPeloNome("Validar cart�o de acesso duplicado");
			if(Boolean.TRUE.equals(Boolean.valueOf(validaCartaoAcessoDuplicado))) {
				boolean cartaoExiste = verificaCartaoAcessoExistente(cartaoAcessoTextField.getText(),
																	visitante.getId() != null ? visitante.getId() : 0l);
				if(cartaoExiste) {
					cartaoAcessoLabel.setText(cartaoAcessoLabel.getText() + " j� existente");
					redAndBoldFont(cartaoAcessoLabel);
					valido = false;
				}
			}
		}
		
		if(matriculaTextField != null
				&& !matriculaTextField.getText().isEmpty()
				&& visitante.getTipo().equals("PEDESTRE")) {
			
			String validaMatriculaDuplicada = buscaParametroPeloNome("Validar matr�culas duplicadas");
			
			if(Boolean.TRUE.equals(Boolean.valueOf(validaMatriculaDuplicada))) {
				boolean matriculaExiste = verificaMatriculaExistente(matriculaTextField.getText(),
																	visitante.getId() != null ? visitante.getId() : 0l);
				
				if(matriculaExiste) {
					matriculaLabel.setText(matriculaLabel.getText() + " j� existente");
					redAndBoldFont(matriculaLabel);
					valido = false;
				}
			}
		}
		
		return valido;
	}

	@SuppressWarnings("unchecked")
	private boolean verificaMatriculaExistente(String matricula, Long idPedestre) {
		HashMap<String, Object> args = new HashMap<>();
		args.put("MATRICULA", matricula);
		args.put("ID_PEDESTRE", idPedestre);
		
		List<PedestrianAccessEntity> pedestres = (List<PedestrianAccessEntity>) HibernateUtil
							.getResultListWithParams(PedestrianAccessEntity.class, "PedestrianAccessEntity.findByMatricula2", args);
		
		return pedestres != null && !pedestres.isEmpty();
	}

	@SuppressWarnings("unchecked")
	private boolean verificaCartaoAcessoExistente(String cartaoAcesso, Long idPedestre) {
		HashMap<String, Object> args = new HashMap<>();
		args.put("CARD_NUMBER", cartaoAcesso);
		args.put("ID_PEDESTRE", idPedestre);
		
		List<PedestrianAccessEntity> pedestres = (List<PedestrianAccessEntity>) HibernateUtil
							.getResultListWithParams(PedestrianAccessEntity.class, "PedestrianAccessEntity.findByCartaoAcesso", args);

		return pedestres != null && !pedestres.isEmpty();
	}

	@SuppressWarnings("unchecked")
	private boolean verificaRgExistente(String rg, Long idPedestre) {
		HashMap<String, Object> args = new HashMap<>();
		args.put("RG", rg);
		args.put("ID_PEDESTRE", idPedestre);
		
		List<PedestrianAccessEntity> pedestres = (List<PedestrianAccessEntity>) HibernateUtil
							.getResultListWithParams(PedestrianAccessEntity.class, "PedestrianAccessEntity.findByRG", args);

		return pedestres != null && !pedestres.isEmpty();
	}

	@SuppressWarnings("unchecked")
	private boolean verificaCpfExistente(String cpf, Long idPedestre) {
		HashMap<String, Object> args = new HashMap<>();
		args.put("CPF", cpf);
		args.put("ID_PEDESTRE", idPedestre);
		
		List<PedestrianAccessEntity> pedestres = (List<PedestrianAccessEntity>) HibernateUtil
							.getResultListWithParams(PedestrianAccessEntity.class, "PedestrianAccessEntity.findByCPF", args);

		return pedestres != null && !pedestres.isEmpty();
	}

	@SuppressWarnings("unchecked")
	private String buscaParametroPeloNome(String nome) {
		HashMap<String, Object> args = new HashMap<>();
		args.put("NOME_PARAM", nome);
		args.put("ID_CLIENTE", Main.loggedUser.getIdClient());
		
		List<ParametroEntity> parametros = (List<ParametroEntity>) HibernateUtil
									.getResultListWithParams(ParametroEntity.class, "ParametroEntity.findByName", args);

		if(parametros == null || parametros.isEmpty())
			return "";
		
		return parametros.get(0).getValor();
	}
	
	private PedestreRegraEntity buscaPedestreRegraPadraoVisitante() {
		
		RegraEntity regra = buscaRegraPeloNome("ACESSO_UNICO_VISITANTE");
		
		if(regra == null)
			regra = cadastraNovaRegra("ACESSO_UNICO_VISITANTE");
		
		PedestreRegraEntity pedestreRegra = new PedestreRegraEntity();
		pedestreRegra.setId(new Date().getTime());
		pedestreRegra.setRegra(regra);
		pedestreRegra.setQtdeTotalDeCreditos(1l);
		pedestreRegra.setCadastradoNoDesktop(true);

		return pedestreRegra;
	}

	private RegraEntity cadastraNovaRegra(String nomeRegra) {
		RegraEntity regra = new RegraEntity();
		regra.setId(new Date().getTime());
		regra.setNome(nomeRegra);
		regra.setTipoPedestre(TipoPedestre.VISITANTE);
		regra.setTipo(TipoRegra.ACESSO_UNICO);
		regra.setIdClient(Main.loggedUser.getIdClient());
		regra.setCadastradoNoDesktop(true);
		
		regra = (RegraEntity) HibernateUtil.save(RegraEntity.class, regra)[0];
		
		return regra;
	}

	@SuppressWarnings("unchecked")
	private RegraEntity buscaRegraPeloNome(String nomeRegra) {
		HashMap<String, Object> args = new HashMap<>();
		args.put("NOME_REGRA", nomeRegra);
		args.put("CADASTRADO_NO_DESKTOP", false);
		
		List<RegraEntity> regras = (List<RegraEntity>) HibernateUtil.getResultListWithParams(RegraEntity.class, "RegraEntity.findByNome", args);
		
		if(regras != null && !regras.isEmpty())
			return regras.get(0);
		
		args = new HashMap<>();
		args.put("NOME_REGRA", nomeRegra);
		args.put("CADASTRADO_NO_DESKTOP", true);
		
		regras = (List<RegraEntity>) HibernateUtil.getResultListWithParams(RegraEntity.class, "RegraEntity.findByNome", args);
		
		return regras.stream().findFirst().orElse(null);
	}

	private void limparTodosOsCampos() {
		nomeTextField.setText("");
		dataNascimentoTextField.setText("");
		emailTextField.setText("");
		cpfTextField.setText("");
		generoJComboBox.setSelectedIndex(0);
		rgTextField.setText("");
		telefoneTextField.setText("");
		celularTextField.setText("");
		responsavelTextField.setText("");
		obsTextArea.setText("");
		statusJComboBox.setSelectedIndex(0);
		//cartaoAcessoTextField.setText("");
		cartaoAcessoTextField.setText(StringUtils.leftPad("0", qtdeDigitosCartao, '0'));
		cepTextField.setText("");
		logradouroTextField.setText("");
		numeroTextField.setText("");
		complementoTextField.setText("");
		bairroTextField.setText("");
		cidadeTextField.setText("");
		estadoTextField.setText("");
		
		new Thread() {
			public void run() {
				Runtime.getRuntime().gc();
			};
		}.start();
		
		
	}

	private void criarDialogoEscolherFoto() {
		escolherFotoDialog = new JDialog();
		escolherFotoDialog.setIconImage(Main.favicon);
		escolherFotoDialog.setModal(true);
		escolherFotoDialog.setTitle("Escolher foto");
		escolherFotoDialog.setResizable(false);

		JPanel escolherFotoPanel = new JPanel();
		escolherFotoPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
		escolherFotoPanel.setLayout(new BoxLayout(escolherFotoPanel, BoxLayout.Y_AXIS));

		JButton escolhertirarFotoButton = new JButton("Tirar foto");
		escolhertirarFotoButton.setBorder(new EmptyBorder(10, 20, 10, 20));
		escolhertirarFotoButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		escolhertirarFotoButton.addActionListener(e -> {
			criarDialogoTirarFoto();
		});

		JButton escolherDoArquivoButton = new JButton("Escolher do arquivo");
		escolherDoArquivoButton.setBorder(new EmptyBorder(10, 20, 10, 20));
		escolherDoArquivoButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		escolherDoArquivoButton.addActionListener(e -> {
			criarFileChooserEscolherFoto();
		});

		JButton cancelarButton = new JButton("Cancelar");
		cancelarButton.setBorder(new EmptyBorder(10, 20, 10, 20));
		cancelarButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		cancelarButton.addActionListener(e -> {
			escolherFotoDialog.dispose();
		});

		escolherFotoPanel.add(escolhertirarFotoButton);
		escolherFotoPanel.add(Box.createVerticalStrut(10));
		escolherFotoPanel.add(escolherDoArquivoButton);
		escolherFotoPanel.add(Box.createVerticalStrut(20));
		escolherFotoPanel.add(cancelarButton);

		escolherFotoDialog.getContentPane().add(escolherFotoPanel);
		escolherFotoDialog.pack();
		escolherFotoDialog.setLocationRelativeTo(null);
		escolherFotoDialog.setVisible(true);
	}

	private void criarFileChooserEscolherFoto() {
		FileDialog fd = new FileDialog(new JFrame(), "Escolha uma imagem", FileDialog.LOAD);
		fd.setDirectory("C:\\");
		fd.setFile("*.jpg;*.jpeg;*.png");
		fd.setVisible(true);
		
		String caminho = fd.getDirectory() + fd.getFile();
		
		if (caminho != null && !"".equals(caminho)) {
			
			if(caminho.endsWith(".png") || caminho.endsWith(".jpg") || caminho.endsWith(".jpeg"))
					criarDialogoCropImage(caminho);
		}
	}

	private void criarDialogoCropImage(String caminho) {
		cropImageDialog = new JDialog();
		cropImageDialog.setIconImage(Main.favicon);
		cropImageDialog.setModal(true);
		cropImageDialog.setTitle("Cortar imagem");
		cropImageDialog.setPreferredSize(new DimensionUIResource(600, 600));

		CropImage cropImagePanel = new CropImage();
		cropImagePanel.setLayout(new BoxLayout(cropImagePanel, BoxLayout.Y_AXIS));
		cropImagePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		cropImagePanel.start(caminho);

		JButton cropImageButton = new JButton("Cortar");
		cropImageButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		cropImageButton.setBorder(new EmptyBorder(10, 20, 10, 20));
		cropImageButton.addActionListener(e -> {
			this.fotoVisitante = cropImagePanel.getCropedImage();
			this.bufferedImage = cropImagePanel.getBufferedImage();
			habilitaLabelImagemVisitante();
			cropImageDialog.dispose();
			escolherFotoDialog.dispose();
		});

		cropImageDialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				cropImageDialog.dispose();
			}
		});

		cropImagePanel.add(Box.createVerticalStrut(10));
		cropImagePanel.add(cropImageButton);
		
		cropImageDialog.getContentPane().add(cropImagePanel);
		cropImageDialog.pack();
		cropImageDialog.setLocationRelativeTo(null);
		cropImageDialog.setVisible(true);
	}

	private void criarDialogoTirarFoto() {
		WebCamCaptureViewer webCamCaptureViewer = new WebCamCaptureViewer();
		webCamCaptureViewer.getTirarFotoButton().addActionListener(e -> {
			BufferedImage imageCaptured = webCamCaptureViewer.getWebcam().getImage();
			
			if(imageCaptured != null) {
				setBufferedImage(imageCaptured);
				salvarImagemCapturada();
				habilitaLabelImagemVisitante();
				
				webCamCaptureViewer.dispose();
			}
		});
		
		webCamCaptureViewer.start();
	}

	public void habilitaLabelImagemVisitante() {
		openImageSelectButton.setIcon(new ImageIcon(resizeToIconFotoVisitante(bufferedImage)));
		panelInternoLateral.remove(openImageSelectButton);
		GridBagConstraints c = getNewGridBag(0, 0, 20, 5);
		panelInternoLateral.add(openImageSelectButton, c);
		barraLateralPanel.updateUI();
	}

	public void salvarImagemCapturada() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(bufferedImage, "jpg", baos);
			baos.flush();
			fotoVisitante = baos.toByteArray();
			baos.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private JPanel getNewMiniPanel(JLabel label, JTextComponent text) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		panel.add(label, c);

		c.gridx = 0;
		c.gridy = 1;
		panel.add(text, c);

		return panel;
	}

	private JTextField getNewTextField(int columns) {
		JTextField textField = new JTextField();
		textField.setColumns(columns);
		textField.setMaximumSize(textField.getPreferredSize());

		return textField;
	}

	private JLabel getNewLabel(String nome) {
		JLabel label = new JLabel(nome);
		return label;
	}

	private Font getDefaultFont() {
		Font font = new JLabel().getFont();
		return font;
	}

	private void loadImages() {
		try {
			Toolkit toolkit = Toolkit.getDefaultToolkit();
			escolherImagemIcon = new ImageIcon(
					toolkit.getImage(Main.class.getResource(Constants.IMAGE_FOLDER + "comuns/ic_photo_male.png")));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void completaComZeros(KeyEvent e) {
		if (cartaoAcessoTextField.getText().length() < qtdeDigitosCartao - 1) {
			cartaoAcessoTextField
					.setText(StringUtils.leftPad(cartaoAcessoTextField.getText(), qtdeDigitosCartao - 1, '0'));
		}

		if (Character.isDigit(e.getKeyChar()) && (cartaoAcessoTextField.getText().charAt(0) == '0')) {
			String valor = "";
			for (int i = 1; i < qtdeDigitosCartao; i++) {
				valor += cartaoAcessoTextField.getText().charAt(i);
			}
			cartaoAcessoTextField.setText(valor);
		} else if (e.getKeyChar() == KeyEvent.VK_BACK_SPACE) {
			cartaoAcessoTextField.setText("0" + cartaoAcessoTextField.getText());
		} else {
			e.consume();
		}
	}

	private GridBagConstraints getNewGridBag(int x, int y, int iY, int iX) {
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = x;
		c.gridy = y;
		c.ipady = iY;
		c.ipadx = iX;
		c.anchor = GridBagConstraints.LINE_START;

		return c;
	}

	private Image resizeToIconFotoVisitante(BufferedImage srcImg) {
		// calcula o deslocamento da imagem e a nova altura para manter a proporcao
		double fatorDeReducao = srcImg.getWidth() / ICON_SIZE;
//		int novaAltura = (int) (srcImg.getHeight() / fatorDeReducao);
		int novaAltura = (int) (srcImg.getWidth() / fatorDeReducao);
		int y = (ICON_SIZE - novaAltura) / 2;

		// cria uma nova imagem redimensionada a partir da original
		BufferedImage resizedImg = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g2 = resizedImg.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(srcImg, 0, y, ICON_SIZE, novaAltura, null);
		g2.dispose();
		return resizedImg;
	}

	private boolean possuiCameraDisponivel() {
		boolean possuiCamera = false;

		if (Main.devicesList != null && !Main.devicesList.isEmpty()) {
			for (Device device : Main.devicesList) {
				if (device instanceof FacialDevice && device.isConnected())
					return true;
			}
		}
		return possuiCamera;
	}

	private boolean possuiCatracaDisponivel() {
		boolean possuiCatraca = false;

		if (Main.devicesList != null && !Main.devicesList.isEmpty()) {
			for (Device device : Main.devicesList) {
				if (!(device instanceof FacialDevice) && device.isConnected())
					return true;
			}
		}
		return possuiCatraca;
	}

	private boolean isEditandoVisitante() {
		return visitante != null && visitante.getId() != null;
	}

	public static void criarDialogoUsuarioNaoPermitidoCadastrarFace() {
		JDialog visitanteNaoDisponivelParaCadastroDialog = new JDialog();
		visitanteNaoDisponivelParaCadastroDialog.setIconImage(Main.favicon);
		visitanteNaoDisponivelParaCadastroDialog.setModal(true);
		visitanteNaoDisponivelParaCadastroDialog.setTitle("Cadastro n�o dispon�vel");
		visitanteNaoDisponivelParaCadastroDialog.setResizable(false);
		visitanteNaoDisponivelParaCadastroDialog.setLayout(new BorderLayout());

		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		JLabel mensagemLabel = new JLabel("Este visitante n�o est� dispon�vel para cadastro de faces.");
		JLabel mensagemLabel2 = new JLabel("Fa�a a sincroniza��o com o sistema web antes de cadastrar faces.");
		mensagemLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		mensagemLabel2.setAlignmentX(Component.CENTER_ALIGNMENT);

		JButton okButton = new JButton("Ok");
		okButton.setBorder(new EmptyBorder(10, 20, 10, 20));
		okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		okButton.addActionListener(e -> {
			visitanteNaoDisponivelParaCadastroDialog.dispose();
		});

		mainPanel.add(mensagemLabel);
		mainPanel.add(mensagemLabel2);
		mainPanel.add(Box.createVerticalStrut(20));
		mainPanel.add(okButton);

		visitanteNaoDisponivelParaCadastroDialog.getContentPane().add(mainPanel, BorderLayout.CENTER);
		visitanteNaoDisponivelParaCadastroDialog.pack();
		visitanteNaoDisponivelParaCadastroDialog.setLocationRelativeTo(null);
		visitanteNaoDisponivelParaCadastroDialog.setVisible(true);
	}

	private void criarDialogoEscolherCameraParaCadastroFace() {
		JDialog escolherCameraParaCadastroFace = new JDialog();
		escolherCameraParaCadastroFace.setIconImage(Main.favicon);
		escolherCameraParaCadastroFace.setModal(true);
		escolherCameraParaCadastroFace.setTitle("Escolha o dispositivo");
		escolherCameraParaCadastroFace.setResizable(false);
		escolherCameraParaCadastroFace.setLayout(new BorderLayout());

		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		JLabel mensagemLabel = new JLabel("Escolha a camera para cadastro!");
		mensagemLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		Vector<SelectItem> itens = new Vector<SelectItem>();
		for (Device device : Main.devicesList) {
			if (device instanceof FacialDevice && device.isConnected()) {
				itens.add(new SelectItem(device.getName(), device));
			}
		}
		JComboBox<SelectItem> escolherCamera = new JComboBox<SelectItem>(itens);

		JButton confirmarButton = new JButton("Confirmar");
		confirmarButton.setBorder(new EmptyBorder(10, 20, 10, 20));
		confirmarButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		confirmarButton.addActionListener(e -> {
			SelectItem itemSelecionado = (SelectItem) escolherCamera.getSelectedItem();
			Device deviceSelecionado = (Device) itemSelecionado.getValue();
			escolherCameraParaCadastroFace.dispose();
			FacialDialog facialDialog = new FacialDialog(Main.mainScreen, deviceSelecionado, this.visitante);
			facialDialog.showScreen();
		});

		mainPanel.add(mensagemLabel);
		mainPanel.add(Box.createVerticalStrut(5));
		mainPanel.add(escolherCamera);
		mainPanel.add(Box.createVerticalStrut(20));
		mainPanel.add(confirmarButton);

		escolherCameraParaCadastroFace.getContentPane().add(mainPanel, BorderLayout.CENTER);
		escolherCameraParaCadastroFace.pack();
		escolherCameraParaCadastroFace.setLocationRelativeTo(null);
		escolherCameraParaCadastroFace.setVisible(true);
	}

	private void criarDialogoEscolherCatracaParaCadastroDigital() {
		JDialog escolherDeviceParaCadastroDigital = new JDialog();
		escolherDeviceParaCadastroDigital.setIconImage(Main.favicon);
		escolherDeviceParaCadastroDigital.setModal(true);
		escolherDeviceParaCadastroDigital.setTitle("Escolha o dispositivo");
		escolherDeviceParaCadastroDigital.setResizable(false);
		escolherDeviceParaCadastroDigital.setLayout(new BorderLayout());

		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		JLabel mensagemLabel = new JLabel("Escolha o device para cadastro!");
		mensagemLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		Vector<SelectItem> itens = new Vector<SelectItem>();
		for (Device device : Main.devicesList) {
			if (!(device instanceof FacialDevice) && !(device instanceof ServerDevice) && device.isConnected()) {
				itens.add(new SelectItem(device.getName(), device));
			}
		}
		JComboBox<SelectItem> escolherDevice = new JComboBox<SelectItem>(itens);

		JButton confirmarButton = new JButton("Confirmar");
		confirmarButton.setBorder(new EmptyBorder(10, 20, 10, 20));
		confirmarButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		confirmarButton.addActionListener(e -> {
			SelectItem itemSelecionado = (SelectItem) escolherDevice.getSelectedItem();
			Device deviceSelecionado = (Device) itemSelecionado.getValue();
			escolherDeviceParaCadastroDigital.dispose();

			RegisterUserDialog rUser = new RegisterUserDialog(Main.mainScreen, deviceSelecionado);
			if (deviceSelecionado.isCatraca()) {
				// exibe apenas um pequeno dialog na tela. O processo � acompanhado no visor da catraca
				rUser.cadastrarOuRemoverUsuario("CADASTRO", this.visitante);
			} else {
				// exibe a tela de coleta de biometria
				rUser.coletar(this.visitante);
			}
		});

		mainPanel.add(mensagemLabel);
		mainPanel.add(Box.createVerticalStrut(5));
		mainPanel.add(escolherDevice);
		mainPanel.add(Box.createVerticalStrut(20));
		mainPanel.add(confirmarButton);

		escolherDeviceParaCadastroDigital.getContentPane().add(mainPanel, BorderLayout.CENTER);
		escolherDeviceParaCadastroDigital.pack();
		escolherDeviceParaCadastroDigital.setLocationRelativeTo(null);
		escolherDeviceParaCadastroDigital.setVisible(true);
	}

	private void criarDialogoConfirmarAddCredito() {
		JDialog confirmarAdicaoCreditoDialog = new JDialog();
		confirmarAdicaoCreditoDialog.setIconImage(Main.favicon);
		confirmarAdicaoCreditoDialog.setModal(true);
		confirmarAdicaoCreditoDialog.setTitle("Confirmar");
		confirmarAdicaoCreditoDialog.setResizable(false);
		confirmarAdicaoCreditoDialog.setLayout(new BorderLayout());

		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		JLabel mensagemLabel = new JLabel("Tem certeza que deseja adicionar um cr�dito para este visitante?");
		mensagemLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		JButton simButton = new JButton("Sim");
		simButton.setBorder(new EmptyBorder(10, 20, 10, 20));
		simButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		simButton.addActionListener(e -> {
			visitante.setQuantidadeCreditos(1l);

			boolean valido = validarCampos();

			if (valido) {
				adicionarVisitante();
				limparTodosOsCampos();
				confirmarAdicaoCreditoDialog.dispose();
				this.dispose();
				
				if(Utils.getPreferenceAsBoolean("pedestrianAlwaysOpen")) {
					new Thread() {
						public void run() {
							Utils.sleep(500);
							if("VISITANTE".equals(visitante.getTipo()))
								Main.mainScreen.abreCadastroVisitante();
							else
								Main.mainScreen.abreCadastroPedestre();
						}
					}.start();
				}else {
					Main.mainScreen.refreshAll();
				}
			}
		});

		JButton naoButton = new JButton("N�o");
		naoButton.setBorder(new EmptyBorder(10, 20, 10, 20));
		naoButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		naoButton.addActionListener(e -> {
			confirmarAdicaoCreditoDialog.dispose();
		});

		JPanel confirmarPanel = new JPanel();
		confirmarPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
		confirmarPanel.setLayout(new BoxLayout(confirmarPanel, BoxLayout.X_AXIS));
		confirmarPanel.add(simButton);
		confirmarPanel.add(Box.createHorizontalStrut(10));
		confirmarPanel.add(naoButton);

		mainPanel.add(mensagemLabel);
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add(confirmarPanel);

		confirmarAdicaoCreditoDialog.getContentPane().add(mainPanel, BorderLayout.CENTER);
		confirmarAdicaoCreditoDialog.pack();
		confirmarAdicaoCreditoDialog.setLocationRelativeTo(null);
		confirmarAdicaoCreditoDialog.setVisible(true);
	}

	
	private boolean isBotaoAddCreditoVisivel() {
		if(visitante != null && visitante.getId() != null
				&& "VISITANTE".equals(visitante.getTipo())) {
			if(visitante.getQuantidadeCreditos() == null)
				return true;
			else if(visitante.getQuantidadeCreditos() != null
					&& visitante.getQuantidadeCreditos() == 0l)
				return true;
		}
		return false;
	}

	private byte[] createMiniImage(byte[] original) {
		try {
			BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(original));
			int sizeImage = 96; // em px
			BufferedImage clipedImage = new BufferedImage(sizeImage, sizeImage, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g1 = clipedImage.createGraphics();
			g1.setClip(new RoundRectangle2D.Double(0, 0, sizeImage, sizeImage, 5, 5));
			g1.drawImage(originalImage, 0, 0, sizeImage, sizeImage, null);
			g1.dispose();

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ImageIO.write(clipedImage, "png", bos);
			return bos.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return original;
	}
	
	private void criaPainelComboBox(JLabel label, JComboBox<SelectItem> comboBox, JPanel mainPanel, int x, int y) {
		comboBox.setPreferredSize(new Dimension(200, 30));
		JPanel internoPanel = new JPanel(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		internoPanel.add(label, c);
		c.gridx = 0;
		c.gridy = 1;
		internoPanel.add(comboBox, c);
		mainPanel.add(internoPanel, getNewGridBag(x, y, 30, 5));
	}
	
	private Long getValorSelecionado(JComboBox<SelectItem> itemComboBox) {
		SelectItem itemSelecionado = (SelectItem) itemComboBox.getSelectedItem();
		
		if(itemSelecionado == null)
			return null;
		
		return (Long) itemSelecionado.getValue();
	}
	
	@SuppressWarnings("unchecked")
	private Vector<SelectItem> getAllCargosSelectItens(Long idEmpresa) {
		Vector<SelectItem> cargoItens = new Vector<SelectItem>();
		cargoItens.add(new SelectItem("Selecione", null));
		List<CargoEntity> cargos = null;

		HashMap<String, Object> args = new HashMap<>();
		args.put("ID_EMPRESA", idEmpresa);

		cargos = (List<CargoEntity>) HibernateUtil.getResultListWithParams(CargoEntity.class, "CargoEntity.findAllByIdEmpresa", args);

		if (cargos == null || cargos.isEmpty())
			return cargoItens;

		cargos.forEach(cargo -> {
			cargoItens.add(new SelectItem(cargo.getNome(), cargo.getId()));
		});

		return cargoItens;
	}

	@SuppressWarnings("unchecked")
	private Vector<SelectItem> getAllCentroCustosSelectItens(Long idEmpresa) {
		Vector<SelectItem> centroCustoItens = new Vector<SelectItem>();
		centroCustoItens.add(new SelectItem("Selecione", null));
		List<CentroCustoEntity> centroCustos = null;

		HashMap<String, Object> args = new HashMap<>();
		args.put("ID_EMPRESA", idEmpresa);

		centroCustos = (List<CentroCustoEntity>) HibernateUtil
						.getResultListWithParams(CentroCustoEntity.class, "CentroCustoEntity.findAllByIdEmpresa", args);

		if (centroCustos == null || centroCustos.isEmpty())
			return centroCustoItens;

		centroCustos.forEach(centro -> {
			centroCustoItens.add(new SelectItem(centro.getNome(), centro.getId()));
		});

		return centroCustoItens;
	}

	@SuppressWarnings("unchecked")
	private Vector<SelectItem> getAllDepartamentosSelectItens(Long idEmpresa) {
		Vector<SelectItem> departamentoItens = new Vector<SelectItem>();
		departamentoItens.add(new SelectItem("Selecione", null));
		List<DepartamentoEntity> departamentos = null;

		HashMap<String, Object> args = new HashMap<>();
		args.put("ID_EMPRESA", idEmpresa);

		departamentos = (List<DepartamentoEntity>) HibernateUtil
						.getResultListWithParams(DepartamentoEntity.class, "DepartamentoEntity.findAllByIdEmpresa", args);

		if (departamentos == null || departamentos.isEmpty())
			return departamentoItens;

		departamentos.forEach(departamento -> {
			departamentoItens.add(new SelectItem(departamento.getNome(), departamento.getId()));
		});

		return departamentoItens;
	}

	@SuppressWarnings("unchecked")
	private Vector<SelectItem> getAllEmpresasSelectItens() {
		Vector<SelectItem> empresaItens = new Vector<SelectItem>();
		empresaItens.add(new SelectItem("Selecione", null));
		List<EmpresaEntity> empresas = null;

		empresas = (List<EmpresaEntity>) HibernateUtil.getResultList(EmpresaEntity.class,
							"EmpresaEntity.findAllActive");
		
		if (empresas == null || empresas.isEmpty())
			return empresaItens;

		empresas.forEach(empresa -> {
			empresaItens.add(new SelectItem(empresa.getNome(), empresa.getId()));
		});

		return empresaItens;
	}
	
	private void redAndBoldFont(JLabel label) {
		label.setForeground(Color.red);
		Font f = label.getFont();
		label.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
	}
	
	private void blackAndUnboldFont(JLabel label) {
		label.setForeground(Color.BLACK);
		Font f = label.getFont();
		label.setFont(f.deriveFont(f.getStyle() & ~Font.BOLD));
	}

	private void restauraFontLabel() {
		blackAndUnboldFont(nomeLabel);
		blackAndUnboldFont(dataNascimentoLabel);
		blackAndUnboldFont(emailLabel);
		blackAndUnboldFont(cpfLabel);
		blackAndUnboldFont(rgLabel);
		blackAndUnboldFont(telefoneLabel);
		blackAndUnboldFont(celularLabel);
		blackAndUnboldFont(obsLabel);
		blackAndUnboldFont(responsavelLabel);
		blackAndUnboldFont(empresaLabel);
		blackAndUnboldFont(departamentoLabel);
		blackAndUnboldFont(cargoLabel);
		blackAndUnboldFont(centroCustoLabel);
		blackAndUnboldFont(cepLabel);
		blackAndUnboldFont(cartaoAcessoLabel);
		blackAndUnboldFont(matriculaLabel);
	}
	
	private boolean isHabilitaCampoMatricula() {
		String matriculaSequencial = buscaParametroPeloNome("Gerar matricula sequencial");
		
		if(matriculaSequencial != null
					&& !matriculaSequencial.isEmpty()) {
			return !Boolean.valueOf(matriculaSequencial);
		}
		return true;
	}

	private boolean isExibeCampoMatricula() {
		String permiteCampoAdicionalMatricula = buscaParametroPeloNome("Permitir campo adicional de crach�/matricula");
		
		if(permiteCampoAdicionalMatricula != null
				&& !permiteCampoAdicionalMatricula.isEmpty()) {
			
			return "PEDESTRE".equals(visitante.getTipo())
					&& Boolean.valueOf(permiteCampoAdicionalMatricula);
		}
		
		return false;
	}
	
	@Override
	public void dispose() {
		abertoPeloAtalho = false;
		super.dispose();
	}

	public BufferedImage getBufferedImage() {
		return bufferedImage;
	}

	public void setBufferedImage(BufferedImage bufferedImage) {
		this.bufferedImage = bufferedImage;
	}
}