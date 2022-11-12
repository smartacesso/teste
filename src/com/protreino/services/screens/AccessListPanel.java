package com.protreino.services.screens;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import com.protreino.services.entity.PedestrianAccessEntity;
import com.protreino.services.main.Main;
import com.protreino.services.utils.HibernateUtil;
import com.protreino.services.utils.SelectItem;
import com.protreino.services.utils.Utils;

@SuppressWarnings("serial")
public class AccessListPanel extends JPanel {

	private JLabel countLabel;
	
	private JTable accessListTable;
	private List<PedestrianAccessEntity> listaAcesso;
	private String[] columns = {"C�digo", "Cart�o", "Nome", "Tipo", "Status", "Regra", "Liberar acesso"};
	private Integer[] columnWidths = {60, 70, 280, 80, 100, 190, 105};
	
	private JTextField filtroIdTextField;
	private JTextField filtroCartaoTextField;
	private JTextField filtroNomeTextField;
	private JComboBox<SelectItem> filtroTipoJComboBox;

	private JButton cleanButton;
	private JButton syncButton;
	private HashMap<String, Object> args;
	private List<Integer> colunasComLink;
	private JLabel dateLastSync;
	private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	
	public AccessListPanel(){
		
		args = new HashMap<String, Object>();
		
		colunasComLink = new ArrayList<Integer>();
		colunasComLink.add(4);
		colunasComLink.add(6);
		
		Font font = new JLabel().getFont();
		Font headerFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
		
		JPanel filtroIdPanel= new JPanel();
		filtroIdPanel.setLayout(new BoxLayout(filtroIdPanel, BoxLayout.Y_AXIS));
		JLabel filtroIdLabel = new JLabel("C�digo");
		filtroIdLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		filtroIdPanel.add(filtroIdLabel);
		filtroIdTextField = new JTextField("", 8);
		filtroIdTextField.setAlignmentX(Component.LEFT_ALIGNMENT);
		filtroIdPanel.add(filtroIdTextField);
		filtroIdPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		JPanel filtroCartaoPanel= new JPanel();
		filtroCartaoPanel.setLayout(new BoxLayout(filtroCartaoPanel, BoxLayout.Y_AXIS));
		JLabel filtroCartaoLabel = new JLabel("Cart�o");
		filtroCartaoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		filtroCartaoPanel.add(filtroCartaoLabel);
		filtroCartaoTextField = new JTextField("", 8);
		filtroCartaoTextField.setAlignmentX(Component.LEFT_ALIGNMENT);
		filtroCartaoPanel.add(filtroCartaoTextField);
		filtroCartaoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		JPanel filtroNomePanel= new JPanel();
		filtroNomePanel.setLayout(new BoxLayout(filtroNomePanel, BoxLayout.Y_AXIS));
		JLabel filtroNomeLabel = new JLabel("Nome");
		filtroNomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		filtroNomePanel.add(filtroNomeLabel);
		filtroNomeTextField = new JTextField("", System.getProperty("os.name").toLowerCase().contains("linux") ? 20 : 35);
		filtroNomeTextField.setAlignmentX(Component.LEFT_ALIGNMENT);
		filtroNomePanel.add(filtroNomeTextField);
		filtroNomePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
	
		JPanel filtroTipoPanel= new JPanel();
		filtroTipoPanel.setLayout(new BoxLayout(filtroTipoPanel, BoxLayout.Y_AXIS));
		JLabel filtroTipoLabel = new JLabel("Tipo");
		filtroTipoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		filtroTipoPanel.add(filtroTipoLabel);
		
		Vector<SelectItem> itens = new Vector<SelectItem>();
		itens.add(new SelectItem("Todos", "TODOS"));
		itens.add(new SelectItem("PEDESTRE", "PEDESTRE"));
		itens.add(new SelectItem("VISITANTE", "VISITANTE"));
		
		filtroTipoJComboBox =  new JComboBox<SelectItem>(itens);
		filtroTipoJComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		filtroTipoPanel.add(filtroTipoJComboBox);
		filtroTipoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		JPanel cleanButtonPanel= new JPanel();
		cleanButtonPanel.setLayout(new BoxLayout(cleanButtonPanel, BoxLayout.Y_AXIS));
		cleanButton = new JButton("Limpar filtros");
		cleanButtonPanel.add(new JLabel(" "));
		cleanButtonPanel.add(cleanButton);
		
		JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		filterPanel.setMaximumSize(new Dimension(10000, 40));
		filterPanel.add(filtroIdPanel);
		filterPanel.add(Box.createHorizontalStrut(10));
		filterPanel.add(filtroCartaoPanel);
		filterPanel.add(Box.createHorizontalStrut(10));
		filterPanel.add(filtroNomePanel);
		filterPanel.add(Box.createHorizontalStrut(10));
		filterPanel.add(filtroTipoPanel);
		filterPanel.add(Box.createHorizontalStrut(10));
		filterPanel.add(cleanButtonPanel);
		
		JPanel accessListTablePanel = new JPanel();
		accessListTablePanel.setLayout(new BoxLayout(accessListTablePanel, BoxLayout.Y_AXIS));
		accessListTable = new JTable(new DefaultTableModel(columns, 0));
		formatTable();
		accessListTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		accessListTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		accessListTable.getTableHeader().setReorderingAllowed(false);
		accessListTable.getTableHeader().setOpaque(false);
		accessListTable.getTableHeader().setForeground(Main.firstColor);
		if(!System.getProperty("os.name").toLowerCase().contains("linux"))
			accessListTable.getTableHeader().setBackground(Main.secondColor);
		accessListTable.getTableHeader().setFont(headerFont);
		accessListTable.setRowHeight(30);
		accessListTable.setSelectionBackground(Main.firstColor);
		accessListTable.setSelectionForeground(Color.WHITE);
		TableCellRenderer rendererFromHeader = accessListTable.getTableHeader().getDefaultRenderer();
		JLabel headerLabel = (JLabel) rendererFromHeader;
		headerLabel.setHorizontalAlignment(JLabel.CENTER);
		JScrollPane scrollPane = new JScrollPane(accessListTable);
		scrollPane.getVerticalScrollBar().setUnitIncrement(Integer.valueOf(Utils.getPreference("scrollSpeed")));
		accessListTablePanel.add(scrollPane);
		
		countLabel = new JLabel("N�mero de registros: ");
		dateLastSync = new JLabel(" ");
		syncButton = new JButton("Atualizar lista com o servidor");
		syncButton.setBorder(new EmptyBorder(10,15,10,15));
		syncButton.setPreferredSize(new Dimension(200, 40));

		
		JPanel statusPanel = new JPanel();
		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
		statusPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
		statusPanel.add(countLabel);
		statusPanel.add(Box.createHorizontalGlue());
		statusPanel.add(dateLastSync);
		statusPanel.add(Box.createHorizontalStrut(10));
		statusPanel.add(syncButton);
		statusPanel.add(Box.createHorizontalStrut(10));
		
		updateDateLastSync();
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		add(filterPanel);
		add(Box.createRigidArea(new Dimension(0,5)));
		add(accessListTablePanel);
		add(Box.createRigidArea(new Dimension(0,5)));
		add(statusPanel);
		
		cleanButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cleanFilter();
			}
		});
		
		syncButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sync();
			}
		});
		
		DocumentListener documentListener = new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				warn();
			}
			public void removeUpdate(DocumentEvent e) {
				warn();
			}
			public void insertUpdate(DocumentEvent e) {
				warn();
			}
			public void warn() {
				filterList();
			}
		};
		
		filtroIdTextField.getDocument().addDocumentListener(documentListener);
		filtroCartaoTextField.getDocument().addDocumentListener(documentListener);
		filtroNomeTextField.getDocument().addDocumentListener(documentListener);
		filtroTipoJComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				filterList();
			}
			
		}); // getDocument().addDocumentListener(documentListener);
		
	}
	
	
	@SuppressWarnings("unchecked")
	private void filterList() {
		args = new HashMap<>();
		args.put("removido", false);
		
		if (filtroIdTextField.getText() != null && !"".equals(filtroIdTextField.getText())) {
			Long id = 0l;
			try {
				id = Long.valueOf(filtroIdTextField.getText());
			} catch (Exception e) {}
			args.put("id", id);
		}
		if (filtroCartaoTextField.getText() != null && !"".equals(filtroCartaoTextField.getText()))
			args.put("cardNumber", filtroCartaoTextField.getText());
		if (filtroNomeTextField.getText() != null && !"".equals(filtroNomeTextField.getText()))
			args.put("name", filtroNomeTextField.getText());
		if (filtroTipoJComboBox.getSelectedItem() != null) {
			SelectItem itemSelecionado = (SelectItem)filtroTipoJComboBox.getSelectedItem();
			if(itemSelecionado.getValue() != null){
				if(itemSelecionado.getValue().equals("TODOS")) {
					args.remove("tipo");
				} else {
					args.put("tipo", itemSelecionado.getValue());
				}
			}
		} else {
			args.remove("tipo");
		}
		
		String construtor = " com.protreino.services.entity.PedestrianAccessEntity(obj.id, obj.cardNumber, obj.name, "
				  + "obj.tipo, obj.status, obj.quantidadeCreditos, obj.validadeCreditos, obj.dataInicioPeriodo, obj.dataFimPeriodo) ";
		
		List<PedestrianAccessEntity> listaAcesso = (List<PedestrianAccessEntity>) HibernateUtil.
				getResultListWithDynamicParams(PedestrianAccessEntity.class, construtor, null, null, "name", args);
		
		populateTable(listaAcesso);
	}
	
	@SuppressWarnings("unchecked")
	public void cleanFilter(){
		args = new HashMap<String, Object>();
		filtroIdTextField.setText("");
		filtroCartaoTextField.setText("");
		filtroNomeTextField.setText("");
		filtroTipoJComboBox.setSelectedIndex(0);
		listaAcesso = (List<PedestrianAccessEntity>) HibernateUtil.
				getResultList(PedestrianAccessEntity.class, "PedestrianAccessEntity.findAllNaoRemovidosOrderedToAccessList");
		
		if(Main.internoLoggedUser != null)
			colunasComLink.add(2);
		populateTable(listaAcesso);
	}
	
	public void sync(){
		try {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			if (!Main.updatingAthleteAccessList) {
				Main.syncAthleteAccessList();
				while (Main.updatingAthleteAccessList)
					Thread.sleep(100);
				cleanFilter();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}
	
	
	private void populateTable(List<PedestrianAccessEntity> listaAcesso){
		DefaultTableModel dataModel = new DefaultTableModel(columns, 0) {
			public boolean isCellEditable(int rowIndex, int mColIndex) {
				return false;
			}
		};
		if (listaAcesso != null && !listaAcesso.isEmpty()){
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			for (PedestrianAccessEntity acesso : listaAcesso) {
				Object[] obj = new Object[7];
				obj[0] = acesso.getId();
				obj[1] = (acesso.getCardNumber() != null && !"".equals(acesso.getCardNumber())) ? acesso.getCardNumber() : "-";
				obj[2] = acesso.getName();
				obj[3] = acesso.getTipo() != null ? acesso.getTipo() : "-";
				obj[4] = "ATIVO".equals(acesso.getStatus()) ? "LIBERADO" : "BLOQUEADO";
				obj[5] =  montaLiberado(acesso); //TODO : vericar quantidade de acessos
				obj[6] = "LIBERAR ACESSO";
				dataModel.addRow(obj);
			}
		}
		accessListTable.setModel(dataModel);
		int numAcessos = listaAcesso != null ? listaAcesso.size() : 0;
		countLabel.setText("N�mero de registros: " + numAcessos);
		formatTable();
	}
	
	private String montaLiberado(PedestrianAccessEntity acesso) {
		
		String texto = "--";
		
		if("VISITANTE".equals(acesso.getTipo())) {
			//visitante
			texto = acesso.getCardNumber() == null ? "--" : "Acesso �nico";
			if(acesso.getQuantidadeCreditos() != null && acesso.getQuantidadeCreditos() > 1l) {
				texto = acesso.getQuantidadeCreditos() + "x cr�ditos ";
				if(acesso.getValidadeCreditos() != null)
					texto += " at� " + new SimpleDateFormat("dd/MM/yyyy").format(acesso.getValidadeCreditos());
			}
		} else{
			
			//pedestre
			if(acesso.getQuantidadeCreditos() != null) {
				texto = acesso.getQuantidadeCreditos() + "x cr�ditos ";
				if(acesso.getValidadeCreditos() != null)
					texto += " at� " + new SimpleDateFormat("dd/MM/yyyy").format(acesso.getValidadeCreditos());
			} else if(acesso.getValidadeCreditos() != null) {
				texto += new SimpleDateFormat("dd/MM/yyyy").format(acesso.getValidadeCreditos());
			}
			
			if(acesso.getDataInicioPeriodo() != null && acesso.getDataFimPeriodo() != null) {
				texto = new SimpleDateFormat("dd/MM/yyyy").format(acesso.getDataInicioPeriodo());
				texto += " at� " + new SimpleDateFormat("dd/MM/yyyy").format(acesso.getDataFimPeriodo());

			} else if(acesso.getDataInicioPeriodo() != null) {
				texto = "Inicia em " + new SimpleDateFormat("dd/MM/yyyy").format(acesso.getDataInicioPeriodo());
			}
		}
		
		return texto;
	}

	private void formatTable(){
		UrlRenderer urlRenderer = new UrlRenderer();
		urlRenderer.setHorizontalAlignment(JLabel.CENTER);
		ActionRenderer actionRenderer = new ActionRenderer();
		actionRenderer.setHorizontalAlignment(JLabel.CENTER);
		EditVisitanteRenderer editVisitanteRenderer = null;
		if(Main.internoLoggedUser != null) {
			editVisitanteRenderer = new EditVisitanteRenderer();
			editVisitanteRenderer.setHorizontalAlignment(JLabel.CENTER);
		}
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		
		while (accessListTable.getMouseListeners().length > 2)
			accessListTable.removeMouseListener(accessListTable.
					getMouseListeners()[accessListTable.getMouseListeners().length-1]);
		
		if(Main.internoLoggedUser != null)
			accessListTable.addMouseListener(editVisitanteRenderer);
		accessListTable.addMouseListener(urlRenderer);
		accessListTable.addMouseListener(actionRenderer);

		while (accessListTable.getMouseMotionListeners().length > 2)
			accessListTable.removeMouseMotionListener(accessListTable.
					getMouseMotionListeners()[accessListTable.getMouseMotionListeners().length-1]);
		
		if(Main.internoLoggedUser != null)
			accessListTable.addMouseMotionListener(editVisitanteRenderer);
		accessListTable.addMouseMotionListener(urlRenderer);
		accessListTable.addMouseMotionListener(actionRenderer);
		
		for (int i = 0; i < accessListTable.getColumnCount(); i++){
			TableColumn column = accessListTable.getColumnModel().getColumn(i);
			column.setPreferredWidth(columnWidths[i]);
			
			if (i == 2 && Main.internoLoggedUser != null)
				column.setCellRenderer(editVisitanteRenderer);
			else if (i == 4)
				column.setCellRenderer(urlRenderer);
			else if (i == 6)
				column.setCellRenderer(actionRenderer);
			else
				column.setCellRenderer(centerRenderer);
			
		}
	}

	private static void open(String url) {
		try {
			if (Desktop.isDesktopSupported()) {
				URI uri = new URI(url);
				Desktop.getDesktop().browse(uri);
			}
			else {
				System.out.println("Desktop n�o suportado. Tentando abrir executando a url.dll");
				Runtime rt = Runtime.getRuntime();
				rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean isFiltering(){
		return (filtroIdTextField.getText() != null && !filtroIdTextField.getText().isEmpty())
				|| (filtroCartaoTextField.getText() != null && !filtroCartaoTextField.getText().isEmpty())
				|| (filtroNomeTextField.getText() != null && !filtroNomeTextField.getText().isEmpty())
				|| (filtroTipoJComboBox.getSelectedItem() != null );
	}
	
	public JButton getSyncButton() {
		return syncButton;
	}
	
	public void updateDateLastSync() {
		if (Main.lastSync != null && Main.lastSync > 0) {
			Date date = new Date(Main.lastSync);
			dateLastSync.setText("�ltima sincroniza��o: " + sdf.format(date));
		}
		else
			dateLastSync.setText(" ");
	}

	class UrlRenderer extends DefaultTableCellRenderer implements MouseListener, MouseMotionListener {
		protected int vrow = -1; // viewRowIndex
		protected int vcol = -1; // viewColumnIndex
		protected boolean isRollover;
	    
	    @Override
	    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	        super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
	        String str = Objects.toString(value, "");
	        String color = isSelected ? "white" : "#46b2ca";
	        if (isRolloverCell(table, row, column)) {
	            setText("<html><u><font color='" + color + "'>" + str);
	        }
	        else if (hasFocus) {
	            setText("<html><font color='" + color + "'>" + str);
	        }
	        else {
	            setText(str);
	        }
	        return this;
	    }
	    
	    protected boolean isRolloverCell(JTable table, int row, int column) {
	        return this.vrow == row && this.vcol == column && this.isRollover;
	    }

	    @Override 
	    public void mouseMoved(MouseEvent e) {
	        JTable table = (JTable) e.getComponent();
	        Point pt = e.getPoint();
	        final int prevRow = vrow;
	        final int prevCol = vcol;
	        final boolean prevRollover = isRollover;
	        vrow = table.rowAtPoint(pt);
	        vcol = table.columnAtPoint(pt);
	        if (colunasComLink.contains(vcol)) 
	        	Main.mainScreen.setCursor(new Cursor(Cursor.HAND_CURSOR));
	        else
	        	Main.mainScreen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	        isRollover = colunasComLink.contains(vcol);
	        if (vrow == prevRow && vcol == prevCol && isRollover == prevRollover) {
	            return;
	        }
	        if (!isRollover && !prevRollover) {
	            return;
	        }
	        Rectangle repaintRect;
	        if (isRollover) {
	            Rectangle r = table.getCellRect(vrow, vcol, false);
	            repaintRect = prevRollover ? r.union(table.getCellRect(prevRow, prevCol, false)) : r;
	        }
	        else {
	            repaintRect = table.getCellRect(prevRow, prevCol, false);
	        }
	        table.repaint(repaintRect);
	    }
	    
	    @Override
	    public void mouseExited(MouseEvent e) {
	    	Main.mainScreen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	        JTable table = (JTable) e.getComponent();
	        if (colunasComLink.contains(vcol)) {
	            table.repaint(table.getCellRect(vrow, vcol, false));
	            vrow = -1;
	            vcol = -1;
	            isRollover = false;
	        }
	    }
	    
	    @Override 
	    public void mouseClicked(MouseEvent e) {
	        JTable table = (JTable) e.getComponent();
	        Point pt = e.getPoint();
	        int ccol = table.columnAtPoint(pt);
	        if (ccol == 4) { // && pointInsidePrefSize(table, pt)) {
	            int crow = table.rowAtPoint(pt);
	            String idUser = String.valueOf(table.getValueAt(crow, 0));
				try {
					open(Main.urlApplication + "/paginas/sistema/pedestres/cadastroPedestre.xhtml?id=" + idUser);
				}
				catch (Exception e1) {
					e1.printStackTrace();
				}
	        }
	    }
	    @Override public void mouseDragged(MouseEvent e) { /* not needed */ }
	    @Override public void mouseEntered(MouseEvent e) { /* not needed */ }
	    @Override public void mousePressed(MouseEvent e) { /* not needed */ }
	    @Override public void mouseReleased(MouseEvent e) { /* not needed */ }
	}
	
	class ActionRenderer extends UrlRenderer {
	    
	    @Override 
	    public void mouseClicked(MouseEvent e) {
	        JTable table = (JTable) e.getComponent();
	        Point pt = e.getPoint();
	        int ccol = table.columnAtPoint(pt);
	        if (ccol == 6) { // && pointInsidePrefSize(table, pt)) {
	            int crow = table.rowAtPoint(pt);
	            String idPedestre = String.valueOf(table.getValueAt(crow, 0));
	            
	            if(Main.getDefaultDevice() != null 
            		    && Boolean.TRUE.equals(Main.getDefaultDevice().isConnected())
            			&& Boolean.TRUE.equals(Main.getDefaultDevice().getConfigurationValueAsBoolean("Bloquear sa�da"))) {
	            	new EscolherSentidoLiberarAcessoDialog(Main.getDefaultDevice(), "Liberado pelo sistema", idPedestre);

	            } else {
	            	Main.idAlunoEspecifico = idPedestre;
	            	Main.releaseAccess();
	            }
	            
	        }
	    }
	    @Override public void mouseDragged(MouseEvent e) { /* not needed */ }
	    @Override public void mouseEntered(MouseEvent e) { /* not needed */ }
	    @Override public void mousePressed(MouseEvent e) { /* not needed */ }
	    @Override public void mouseReleased(MouseEvent e) { /* not needed */ }
	}
	
	class EditVisitanteRenderer extends UrlRenderer {

		@Override
		public void mouseClicked(MouseEvent e) {
			JTable table = (JTable) e.getComponent();
			Point pt = e.getPoint();
			int ccol = table.columnAtPoint(pt);
			if (ccol == 2) {
				int crow = table.rowAtPoint(pt);
				String idVisitante = String.valueOf(table.getValueAt(crow, 0));

				args = new HashMap<String, Object>();
				args.put("ID", Long.valueOf(idVisitante));

				PedestrianAccessEntity visitante = (PedestrianAccessEntity) HibernateUtil.getUniqueResultWithParams(
						PedestrianAccessEntity.class, "PedestrianAccessEntity.findById", args);

				new RegisterVisitorDialog(visitante);
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {}
		@Override
		public void mouseEntered(MouseEvent e) {}
		@Override
		public void mousePressed(MouseEvent e) {}
		@Override
		public void mouseReleased(MouseEvent e) {}
	}
	
}
