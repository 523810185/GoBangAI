package practice;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import practice.Board.PlayerColor;

public class GoBang {
	private GoBang() {}
	private static GoBang m_pInstance = null;
	public static GoBang Instance()
	{
		if(m_pInstance == null) 
		{
			m_pInstance = new GoBang();
		}
		
		return m_pInstance;
	}
	
	public static void main(String[] args) {
		new GoBang().init();
	}
	private JFrame jf = new JFrame("������");

	// ����˵�
	private JMenuBar jmb = new JMenuBar();

	// ѡ��˵�
	private JMenu choice = new JMenu("ѡ��");
	private JMenuItem start = new JMenuItem("����Ϸ");
	private JMenuItem exit = new JMenuItem("�˳�");
	private JMenuItem chooseAIColorToBlack = new JMenuItem("��AIִ��"); 
	private JMenuItem chooseAIColorToWhite = new JMenuItem("��AIִ��"); 

	// ���� ���� ���� ���
	private Image board;
	private Image black;
	private Image white;
	private Image selected;

	// ���廭�� ���� ���� ����
	private MyDrawArea db;

	// ���ڵ�����
	private int windowPosX;
	private int windowPosY;

	// ���ڵĴ�С
	private int windowWidth=540;
	private int windowHeight=583;

	// �����Ի���
	private JDialog jd1 = new JDialog(jf, "���", true);
	private JDialog jd2 = new JDialog(jf, "���", true);
	private JLabel win1;
	private JLabel win2;

	// ��¼����λ�� ÿ������ ���Ǵ洢������
	private int selectedX = 0;
	private int selectedY = 0;
	
	public void init() {
		//��ʼ��ͼƬ��Ϣ
		try {
			board =ImageIO.read(new File("ico/board.jpg"));
			black =ImageIO.read(new File("ico/black.gif"));
			white =ImageIO.read(new File("ico/white.gif"));
			selected =ImageIO.read(new File("ico/selected.gif"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		jf.setBounds(100, 200, windowWidth, windowHeight);
		
		choice.add(start);
		choice.add(exit);
		choice.addSeparator();
		choice.add(chooseAIColorToBlack);
		choice.add(chooseAIColorToWhite);
		
		jmb.add(choice);
		jf.setJMenuBar(jmb);
		
		
		db =new MyDrawArea();
		jf.add(db);
		
		//������¿�ʼ��Ϸ�ļ����� �˳��ļ���
		start.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				restart();
			}
		});
		
		exit.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		
		chooseAIColorToBlack.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Board.Instance().SetAIColor(PlayerColor.Black);
			}
		});
		
		chooseAIColorToWhite.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Board.Instance().SetAIColor(PlayerColor.White);
			}
		});
		
		
		//�������  ����   ���ļ��� 
//		db.addMouseMotionListener(new MouseAdapter() {
//			@Override
//			public void mouseMoved(MouseEvent e) {
//				selectedX =(e.getX()-6)/35;
//				selectedY =(e.getY()-6)/35;
////				db.repaint();
//			}
//		});
//		
//		/**
//		 * https://ask.csdn.net/questions/733052
//		 * �ػ�bug
//		 * 
//		 * ���������Ǳ�addMouseMotionListener��drag�¼��ػ���
//		 */
//		db.addMouseListener(new MouseAdapter() {
//			@Override
//			public void mouseClicked(MouseEvent e) {
//				System.out.println("click");
//				int x =(e.getX()-6)/35;
//				int y=(e.getY()-6)/35;
//				put(x,y);
////				db.repaint();
//			}
//		});
		
		MouseHandler mouseHandler = new MouseHandler();
		db.addMouseMotionListener(mouseHandler);
		db.addMouseListener(mouseHandler);
	
		jf.setVisible(true);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		new Thread(){
			@Override
			public void run() {
				while(true)
				{
					db.repaint();
					try {
						sleep(16);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}.start();
		
		restart();
	}
	
	class MouseHandler extends MouseAdapter
	{
		@Override
		public void mouseMoved(MouseEvent e) 
		{
			selectedX =(e.getX()-6)/35;
			selectedY =(e.getY()-6)/35;
		}
		
		@Override 
		public void mousePressed(MouseEvent e) 
		{
			// ע�������Ƿ�������
			int y = (e.getX()-6)/35;
			int x = (e.getY()-6)/35;
			put(x, y);
		}
		
		@Override
		public void mouseReleased(MouseEvent e) 
		{
			
		}
		
		@Override
		public void mouseDragged(MouseEvent e) 
		{
			
		}
	}
	
	/**
	 * ��Ϸ������   �������
	 */
	public boolean put(int x, int y) {
		return Board.Instance().GamerPutAt(x, y);
	}
	
	/**
	 * ��ʼ�µ�һ����Ϸ
	 */
	public void restart() {
		Board.Instance().ResetGame();
	}
	
	/**
	 * ��Ϸ����
	 */
	public void end() {	
		PlayerColor winnerColor = Board.Instance().GetWinnerColor();
		if(winnerColor == Board.PlayerColor.White) {
			JOptionPane.showMessageDialog(null, "����Ӯ");
		}else {
			JOptionPane.showMessageDialog(null, "����Ӯ");
		}
	}
	
	/**
	 * ����
	 */
	class MyDrawArea extends JPanel {
		@Override
		public void paint(Graphics g) {
			// ����ͼƬ
			g.drawImage(board, 0, 0,535,536, null);
			
			char[][] board = Board.Instance().GetBoard();

			// ���ƺ���Ͱ���
			for(int i=0;i<15;i++) {
				for(int j=0;j<15;j++) {
					if(board[j][i]=='1') {//�������Ķ�ά�����е�λ����1  ������ƺ���
						g.drawImage(black, 6+i*35, 6+j*35, 33,33,null);
						
					}else if(board[j][i]=='2') {//�������Ķ�ά�����е�λ����2  ������ư���
						g.drawImage(white, 6+i*35, 6+j*35, 33,33,null);
					}
				}
			}
			
			// ���ƹ��
			g.drawImage(selected, 6+35*selectedX, 6+35*selectedY, 33,33,null);
		}
	}
	
}
