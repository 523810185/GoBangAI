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
	private JFrame jf = new JFrame("五子棋");

	// 定义菜单
	private JMenuBar jmb = new JMenuBar();

	// 选择菜单
	private JMenu choice = new JMenu("选择");
	private JMenuItem start = new JMenuItem("新游戏");
	private JMenuItem exit = new JMenuItem("退出");
	private JMenuItem chooseAIColorToBlack = new JMenuItem("让AI执黑"); 
	private JMenuItem chooseAIColorToWhite = new JMenuItem("让AI执白"); 

	// 棋盘 黑棋 白棋 光标
	private Image board;
	private Image black;
	private Image white;
	private Image selected;

	// 定义画板 绘制 棋盘 棋子
	private MyDrawArea db;

	// 窗口的坐标
	private int windowPosX;
	private int windowPosY;

	// 窗口的大小
	private int windowWidth=540;
	private int windowHeight=583;

	// 结束对话框
	private JDialog jd1 = new JDialog(jf, "结局", true);
	private JDialog jd2 = new JDialog(jf, "结局", true);
	private JLabel win1;
	private JLabel win2;

	// 记录光标的位置 每次下棋 就是存储的内容
	private int selectedX = 0;
	private int selectedY = 0;
	
	public void init() {
		//初始化图片信息
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
		
		//添加重新开始游戏的监听和 退出的监听
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
		
		
		//添加棋盘  棋子   光标的监听 
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
//		 * 重绘bug
//		 * 
//		 * 后来发现是被addMouseMotionListener的drag事件截获了
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
			// 注意这里是反过来的
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
	 * 游戏过程中   玩家下棋
	 */
	public boolean put(int x, int y) {
		return Board.Instance().GamerPutAt(x, y);
	}
	
	/**
	 * 开始新的一局游戏
	 */
	public void restart() {
		Board.Instance().ResetGame();
	}
	
	/**
	 * 游戏结束
	 */
	public void end() {	
		PlayerColor winnerColor = Board.Instance().GetWinnerColor();
		if(winnerColor == Board.PlayerColor.White) {
			JOptionPane.showMessageDialog(null, "白棋赢");
		}else {
			JOptionPane.showMessageDialog(null, "黑棋赢");
		}
	}
	
	/**
	 * 画板
	 */
	class MyDrawArea extends JPanel {
		@Override
		public void paint(Graphics g) {
			// 绘制图片
			g.drawImage(board, 0, 0,535,536, null);
			
			char[][] board = Board.Instance().GetBoard();

			// 绘制黑棋和白棋
			for(int i=0;i<15;i++) {
				for(int j=0;j<15;j++) {
					if(board[j][i]=='1') {//如果虚拟的二维数组中的位置是1  代表绘制黑棋
						g.drawImage(black, 6+i*35, 6+j*35, 33,33,null);
						
					}else if(board[j][i]=='2') {//如果虚拟的二维数组中的位置是2  代表绘制白棋
						g.drawImage(white, 6+i*35, 6+j*35, 33,33,null);
					}
				}
			}
			
			// 绘制光标
			g.drawImage(selected, 6+35*selectedX, 6+35*selectedY, 33,33,null);
		}
	}
	
}
