package practice;

import practice.Board.PlayerColor;

public class ScoreEvaluator 
{
	private ScoreEvaluator() { this.Init(); }
	private static ScoreEvaluator m_pInstance = null;
	public static ScoreEvaluator Instance()
	{
		if(m_pInstance == null) 
		{
			m_pInstance = new ScoreEvaluator();
		}
		
		return m_pInstance;
	}
	
	private Board m_pHandler = null;
	private int m_iBoardSize = 1;
	private void Init() 
	{
		m_iBoardSize = Board.BOARD_SIZE;
		m_pHandler = Board.Instance();
	}
	
	private PlayerColor m_stNowColor = null;
	public ScoreEvaluator SetNowPlayer(PlayerColor nowColor)
	{
		this.m_stNowColor = nowColor;
		return this;
	}
	public static final int MAX = 100000000;
	public static final int MIN = -MAX;
	public static final int GO_FIVE_SCORE = 10000000; 
	public static final int LIVE_FOUR_SCORE = 100000;
	private static final int CHONG_FOUR_SCORE = 10000;
	public static final int LIVE_THREE_SCORE = 10000;
	private static final int SLEEP_THREE_SCORE = 500;
	private static final int LIVE_TWO_SCORE = 20;
	private static final int SLEEP_TWO_SCORE = 10; 
	public int GetBoardScore(boolean isAIPlay)
	{
		ResetVisRecorder();
		SetNowPlayer(Board.Instance().GetAIColor());
		Board ctx = Board.Instance();
		
		int score = 0;
		
		for(int k=0;k<Board.DIR.length;k++) 
		{
			int[] dir = Board.DIR[k];
			int _dirX = dir[0];
			int _dirY = dir[1];
			
			for(int i=0;i<m_iBoardSize;i++) 
			{
				for(int j=0;j<m_iBoardSize;j++) 
				{
					if(m_arrVisRecorder[i][j] == k) 
					{
						continue;
					}
					
					if(ctx.IsEmptyPos(i, j)) 
					{
						continue;
					}
					
					m_arrVisRecorder[i][j] = k;
					int cnt = 1; // 数目
					boolean useEmptyChance = false;
					int _RX = -1; // 最右边的一个同色
					int _RY = -1;
					int _LContinueCnt = 0, _RContinueCnt = 0; // 如果有空格，左右分别有几个
					for(int step=1;step<9;step++) 
					{
						int _testX = i + _dirX * step;
						int _testY = j + _dirY * step;
						if(ctx.IsValidPos(_testX, _testY) == false) 
						{
							break;
						}
						if(ctx.IsEmptyPos(_testX, _testY)) 
						{
							if(!useEmptyChance) 
							{
								if(ctx.IsSameColor(i, j, _testX + _dirX, _testY + _dirY))
								{
									useEmptyChance = true;
									continue;
								}
								else
								{
									break;
								}
							}
							else 
							{
								break;
							}
						}
						
						if(ctx.IsSameColor(i, j, _testX, _testY)) 
						{
							cnt++;
							m_arrVisRecorder[_testX][_testY] = k;
							_RX = _testX;
							_RY = _testY;
							
							if(useEmptyChance) 
							{
								_RContinueCnt++;
							}
							else 
							{
								_LContinueCnt++;
							}
						}
						else 
						{
							break;
						}
					}
					
					// 算分
					boolean isAIColor = ctx.IsAIColorAtPos(i, j);
					int _LoneX = i - _dirX;
					int _LoneY = j - _dirY;
					int _RoneX = _RX + _dirX;
					int _RoneY = _RY + _dirY;
					boolean _LoneIsEmpty = ctx.IsEmptyPos(_LoneX, _LoneY);
					boolean _RoneIsEmpty = ctx.IsEmptyPos(_RoneX, _RoneY);
					float rate = GetScoreRateByIsAIColorAtPos(isAIColor, isAIPlay);
					if(cnt >= 5) 
					{
						if(useEmptyChance) 
						{
							if(_LoneIsEmpty) 
							{
								if(_LContinueCnt >= 5) 
								{
									score += GO_FIVE_SCORE * rate;
								}
								else if(_LContinueCnt == 4) 
								{
									score += LIVE_FOUR_SCORE * rate;
								}
								else if(_LContinueCnt == 3) 
								{
									score += LIVE_THREE_SCORE * rate;
								}
							}
							else if(_RoneIsEmpty) 
							{
								if(_RContinueCnt >= 5) 
								{
									score += GO_FIVE_SCORE * rate;
								}
								else if(_RContinueCnt == 4) 
								{
									score += LIVE_FOUR_SCORE * rate;
								}
								else if(_RContinueCnt == 3) 
								{
									score += LIVE_THREE_SCORE * rate;
								}
							}
							
							score += CHONG_FOUR_SCORE * rate;
						}
						else 
						{
							score += GO_FIVE_SCORE * rate;
						}
					}
					else if(cnt == 4) 
					{
						if(useEmptyChance) 
						{
							score += CHONG_FOUR_SCORE * rate;
						}
						else 
						{
							if(_LoneIsEmpty && _RoneIsEmpty) 
							{
								score += LIVE_FOUR_SCORE * rate;
							}
							else if(!_LoneIsEmpty && !_RoneIsEmpty) 
							{
								// 都被堵住
							}
							else 
							{
								// 被堵住一边
								score += CHONG_FOUR_SCORE * rate;
								// TODO..这里是提高自己下棋以后对敌方冲四的警戒度（虽然可以多一层来搜索出这个位置，但是修改评估函数可以更快剪枝）
								if(isAIPlay && isAIColor == false) 
								{
									score -= LIVE_FOUR_SCORE;
								}
								else if(!isAIPlay && isAIColor) 
								{
									score += LIVE_FOUR_SCORE;
								}
							}
						}
					}
					else if(cnt == 3)
					{
						if(_LoneIsEmpty && _RoneIsEmpty) 
						{
							score += LIVE_THREE_SCORE * rate;
						}
						else if(!_LoneIsEmpty && !_RoneIsEmpty) 
						{
							// 都被堵住
						}
						else 
						{
							// 被堵住一边
							score += SLEEP_THREE_SCORE * rate;
						}
					}
					else if(cnt == 2) 
					{
						if(_LoneIsEmpty && _RoneIsEmpty) 
						{
							score += LIVE_TWO_SCORE * rate;
						}
						else if(!_LoneIsEmpty && !_RoneIsEmpty) 
						{
							// 都被堵住
						}
						else 
						{
							// 被堵住一边
							score += SLEEP_TWO_SCORE * rate;
						}
					}
				}
			}
		}
		
		return score;
	}
	
	private float GetScoreRateByIsAIColorAtPos(boolean isAIColor, boolean isAIPlay) 
	{
		if(isAIPlay) 
		{
			if(isAIColor) 
			{
				return 1f;
			}
			else 
			{
				return -2f;
			}
		}
		else 
		{
			if(isAIColor) 
			{
				return 2f;
			}
			else 
			{
				return -1f;
			}
		}
	}
	
	private int[][] m_arrVisRecorder = new int[Board.BOARD_SIZE][Board.BOARD_SIZE];
	private void ResetVisRecorder()
	{
		for(int i=0;i<m_arrVisRecorder.length;i++) 
		{
			int[] oneDArr = m_arrVisRecorder[i];
			for(int j=0;j<oneDArr.length;j++) 
			{
				oneDArr[j] = -1;
			}
		}
	}
	
	public int GetScoreAtPos(int x, int y, boolean isAIPlay) 
	{
		Board ctx = Board.Instance();
		if(ctx.IsValidPos(x, y) == false || ctx.IsEmptyPos(x, y) == false) 
		{
			// 只评估一个为空的位置(x, y)
			System.out.println("GetScoreAtPos 逻辑错误！");
			return 0;
		}
		
		int score = 0;
		
		for(int __=0;__<Board.DIR.length;__++) 
		{
			int[] dir = Board.DIR[__];
			int _dirX = dir[0];
			int _dirY = dir[1];
			
			// 判定左边
			int cntL = 0;
			int _LoneX = x - _dirX;
			int _LoneY = y - _dirY;
			if(ctx.IsValidPos(_LoneX, _LoneY) && ctx.IsEmptyPos(_LoneX, _LoneY) == false) 
			{
				cntL++;
				for(int i=2;i<=4;i++) 
				{
					int _tx = x - i * _dirX;
					int _ty = y - i * _dirY;
					if(ctx.IsSameColor(_LoneX, _LoneY, _tx, _ty) == false) 
					{
						break;
					}
					
					cntL++;
				}
			}
			// 判定右边
			int cntR = 0;
			int _RoneX = x + _dirX;
			int _RoneY = y + _dirY;
			if(ctx.IsValidPos(_RoneX, _RoneY) && ctx.IsEmptyPos(_RoneX, _RoneY) == false) 
			{
				cntR++;
				for(int i=2;i<=4;i++) 
				{
					int _tx = x + i * _dirX;
					int _ty = y + i * _dirY;
					if(ctx.IsSameColor(_RoneX, _RoneY, _tx, _ty) == false) 
					{
						break;
					}
					
					cntR++;
				}
			}
			
			if(ctx.IsSameColor(_LoneX, _LoneY, _RoneX, _RoneY)) 
			{
				int cntAll = cntL + cntR;
				score += GetPosScoreWithoutColor(cntAll + 1);
			}
			else 
			{
				int cntAll = Math.max(cntL, cntR);
				score += GetPosScoreWithoutColor(cntAll + 1);
			}
		}
		
		return score;
	}
	
	/**
	 * call only by GetScoreAtPos()
	 * @param cntAll
	 * @return
	 */
	private int GetPosScoreWithoutColor(int cntAll) 
	{
		if(cntAll >= 4) 
		{
			return LIVE_FOUR_SCORE;
		}
		else if(cntAll == 3) 
		{
			return LIVE_THREE_SCORE;
		}
		else if(cntAll == 2) 
		{
			return LIVE_TWO_SCORE;
		}
		else
		{
			return cntAll;
		}
	}
}
