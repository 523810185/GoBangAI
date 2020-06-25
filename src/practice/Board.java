package practice;

public class Board 
{
	public enum PlayerColor 
	{
		Black, 
		White,
	}
	
	private Board() {}
	private static Board m_pInstance = null;
	public static Board Instance()
	{
		if(m_pInstance == null) 
		{
			m_pInstance = new Board();
		}
		
		return m_pInstance;
	}
	
	// 存储了当前棋盘的Hash
	private long m_lBoardHash;
	// 把图片拆分成坐标 用来存储下棋的位置
	private char[][] m_stBoard = new char[15][15];
	public final static int BOARD_SIZE = 15;
	//记录下棋的顺序
	private PlayerColor m_eNowColor = PlayerColor.Black;
	// 记录AI使用的颜色
	private PlayerColor m_eAIColor = PlayerColor.Black;
	
	public void SetAIColor(PlayerColor color) 
	{
		this.m_eAIColor = color;
		ResetGame();
	}
	
	public PlayerColor GetAIColor()
	{
		return m_eAIColor;
	}
	
	public PlayerColor GetGamerColor() 
	{
		return GetOppositeColor(m_eAIColor);
	}
	
	public boolean IsAIPlay()
	{
		return m_eNowColor == m_eAIColor;
	}
	
	public boolean IsEmptyPos(int x, int y)
	{
		if(IsValidPos(x, y) == false) 
		{
			return false;
		}
		
		return m_stBoard[x][y] == '0';
	}
	
	public boolean IsValidPos(int x, int y) 
	{
		return 0 <= x && x < BOARD_SIZE &&
				0 <= y && y < BOARD_SIZE;
	}
	
	public boolean IsSameColor(int x1, int y1, int x2, int y2)
	{
		if(!IsValidPos(x1, y1) || !IsValidPos(x2, y2)) 
		{
			return false;
		}
		
		return m_stBoard[x1][y1] == m_stBoard[x2][y2];
	}
	
	public boolean IsAIColorAtPos(int x, int y) 
	{
		if(!IsValidPos(x, y) || IsEmptyPos(x, y)) 
		{
			return false;
		}
		
		return ColorToChar(m_eAIColor) == m_stBoard[x][y];
	}
	
	public class TestSetResult 
	{
		public boolean setSuccess, gameIsEnd;
	}
	
	/**
	 * 搜索中供调用的方法
	 * @param x
	 * @param y
s	 */
	public TestSetResult TestSetAt(int x, int y)
	{
		TestSetResult result = new TestSetResult();
		result.setSuccess = result.gameIsEnd = false;
		if(!IsEmptyPos(x, y))
		{
			return result;
		}
		
		PlayerColor color = m_eNowColor;
		PutChessInner(x, y, color);
		
		result.setSuccess = true;
		result.gameIsEnd = this.IsEndInner(color);
		return result;
	}
	
	/**
	 * 搜索中供取消调用的方法
	 * @param x
	 * @param y
	 */
	public boolean UnsetAt(int x, int y) 
	{
		if(!IsValidPos(x, y) || IsValidPos(x, y) && IsEmptyPos(x, y))
		{
			return false;
		}
		
		UnputChessInner(x, y);
		return true;
	}
	
	public long GetBoardHashCode() 
	{
		return m_lBoardHash;
	}

	public char[][] GetBoard() 
	{
		return m_stBoard;
	}
	
	private void TestAI()
	{
		m_stBoard[7][7] = '1';
		m_stBoard[7][8] = '1';
		m_stBoard[8][7] = '2';
		m_stBoard[8][8] = '2';
		m_stBoard[8][9] = '2';
	}
	
	public void ResetGame() 
	{
		for(int i=0;i<BOARD_SIZE;i++) {
			for(int j=0;j<BOARD_SIZE;j++) {
				m_stBoard[i][j]='0';
			}
		}
		
//		TestAI();
		
		m_eNowColor = Board.PlayerColor.Black;
		
		if(m_eNowColor == m_eAIColor) 
		{
			AI.Instance().DoMove();
		}
		
		m_bGameIsEnd = false;
		m_eWinnerColor = null;
		m_iExistChessCnt = 0;
		m_lBoardHash = 0;
	}
	
	/**
	 * 下棋到某一个位置
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean PutChessAt(int x, int y) 
	{
		if(!IsEmptyPos(x, y))
		{
			return false;
		}
		
		PlayerColor nowColor = m_eNowColor;
		PutChessInner(x, y, nowColor);
		if(IsEndInner(nowColor))
		{
			m_bGameIsEnd = true;
			m_eWinnerColor = nowColor;
			GoBang.Instance().end();
		}
		else 
		{
			PlayerColor nxtColor = m_eNowColor; // 这里已经SwapColor了，所以是nxtColor
			if(nxtColor == m_eAIColor) 
			{
				AI.Instance().DoMove();
			}
		}
		
		return true;
	}
	
	/**
	 * 内部使用的下棋方法
	 * @param x
	 * @param y
	 * @param color
	 */
	private void PutChessInner(int x, int y, PlayerColor color) 
	{
		if(IsEmptyPos(x, y) == false) 
		{
			System.err.println("下棋出错，是个非法点或者不是空位置！" + x + " " + y);
		}
		else 
		{
			m_stBoard[x][y] = this.ColorToChar(color);
			m_lBoardHash = Zobrist.Instance().AppendHashCode(m_lBoardHash, x, y, color);
			m_iExistChessCnt++;
			SwapNowColor();
		}
	}
	
	/**
	 * 内部使用的取消棋子方法
	 * @param x
	 * @param y
	 */
	private void UnputChessInner(int x, int y)
	{
		if(!IsValidPos(x, y) || IsEmptyPos(x, y))
		{
			System.err.println("取消下棋出错，是个非法点或者是空位置！" + x + " " + y);
		}
		else 
		{
			m_lBoardHash = Zobrist.Instance().UnAppendHashCode(m_lBoardHash, x, y, CharToColor(m_stBoard[x][y]));
			m_stBoard[x][y] = '0';
			m_iExistChessCnt--;
			SwapNowColor();
		}
	}
	
	private PlayerColor m_eWinnerColor = null;
	public PlayerColor GetWinnerColor()
	{
		if(!GameIsEnd()) 
		{
			return null;
		}
		
		return m_eWinnerColor;
	}
	private boolean m_bGameIsEnd = false;
	public boolean GameIsEnd() 
	{
		return m_bGameIsEnd;
	}
	private int m_iExistChessCnt = 0;
	public int GetExistChessCnt()
	{
		return m_iExistChessCnt;
	}
	public static final int[][] DIR = {
			{0, 1},
			{1, 0},
			{1, 1},
			{1, -1},
	};
	/**
	 * 判断 当最后一步棋放入完成后是否是五部成棋
	 */
	private boolean IsEndInner(Board.PlayerColor color) 
	{
		char c = ColorToChar(color);
		
		for(int i=0;i<BOARD_SIZE;i++) 
		{
			for(int j=0;j<BOARD_SIZE;j++) 
			{
				if(m_stBoard[i][j] != c) 
				{
					continue;
				}
				
				for(int k=0;k<DIR.length;k++)
				{
					int[] dir = DIR[k];
					int _x = dir[0];
					int _y = dir[1];
					int cnt = 1;
					for(int p=1;p<=4;p++) 
					{
						int _newX = i + p * _x;
						int _newY = j + p * _y;
						if(0 <= _newX && _newX < BOARD_SIZE &&
								0 <= _newY && _newY < BOARD_SIZE) 
						{
							if(m_stBoard[_newX][_newY] == c) 
							{
								cnt++;
							}
						}
					}
					if(cnt == 5) 
					{
//						System.out.println("endGame ! ");
//						for(int __=0;__<=4;__++) 
//						{
//							int _xxx = i + __ * _x;
//							int _yyy = j + __ * _y;
//							System.out.println("?? " + _xxx + " " + _yyy + " " + m_stBoard[_xxx][_yyy]);
//						}
						
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	private PlayerColor GetOppositeColor(PlayerColor color) 
	{
		if(color == PlayerColor.Black) 
		{
			return PlayerColor.White;
		}
		else 
		{
			return PlayerColor.Black;
		}
	}
	private PlayerColor CharToColor(char c) 
	{
		if(c == '1') 
		{
			return PlayerColor.Black;
		}
		else if(c == '2') 
		{
			return PlayerColor.White;
		}
		
		return null;
	}
	private char ColorToChar(PlayerColor color) 
	{
		char c = '0';
		if(color == PlayerColor.Black)
		{
			c = '1';
		}
		else 
		{
			c = '2';
		}
		
		return c;
	}
	private char GetNowColorChar() 
	{
		return ColorToChar(m_eNowColor);
	}
	private void SwapNowColor() 
	{
		m_eNowColor = GetOppositeColor(m_eNowColor);
	}
}
