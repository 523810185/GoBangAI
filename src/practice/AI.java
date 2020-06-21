package practice;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import practice.Board.PlayerColor;
import practice.Board.TestSetResult;
import practice.Zobrist.ScoreNode;

public class AI 
{
	private Board m_stCtx = null; 
	private int m_iBoardSize = 1;
	private AI()
	{
		this.m_stCtx = Board.Instance();
		this.m_iBoardSize = Board.BOARD_SIZE;
	}
	
	private static AI m_pInstance = null;
	public static AI Instance()
	{
		if(m_pInstance == null) 
		{
			m_pInstance = new AI();
		}
		
		return m_pInstance;
	}
	
	class AIThread extends Thread
	{
		@Override
		public void run() 
		{
			if(m_stCtx == null) 
			{
				return;
			}
			
			if(m_stCtx.GetExistChessCnt() == 0) 
			{
				m_stCtx.AIPutAt(7, 7);
				return;
			}
			
			// 迭代加深搜索
			for(int dfsLen = 2; dfsLen <= MAX_DFS_LEN; dfsLen += 2)
			{
				long nowTime = System.currentTimeMillis();
				if(IsOutOfTime()) 
				{
					break;
				}
				DFS(MAX_DFS_LEN - dfsLen, true, alpha, beta);
			}
			
			AfterAISetChess();
			if(!m_stCtx.IsEmptyPos(setX, setY))
			{
				System.out.println("AI DFS 逻辑错误！");
			}
			else 
			{
				m_stCtx.AIPutAt(setX, setY);
			}
		}
	}
	
	/**
	 * 启发式搜索的预处理节点
	 * @author ziyangzhang
	 *
	 */
	private class DFSNode
	{
		public int x, y;
		public int score;
		public DFSNode SetX(int x) { this.x = x; return this; }
		public DFSNode SetY(int y) { this.y = y; return this; }
		public DFSNode SetScore(int score) { this.score = score; return this; }
	}
	
	private class DFSNodePool 
	{
		private List<DFSNode> m_arrPool = new ArrayList<>();
		public void CheckIn(DFSNode node) 
		{
			m_arrPool.add(node);
		}
		public DFSNode CheckOut() 
		{
			if(m_arrPool.isEmpty()) 
			{
				DFSNode node = new DFSNode();
				return node;
			}
			
			DFSNode node = m_arrPool.get(m_arrPool.size() - 1);
			m_arrPool.remove(m_arrPool.size() - 1);
			return node;
		}
	}
	
	private class DFSResultNode
	{
		public int score;
		public boolean canUse = false; // 是否可用
		public DFSResultNode SetScore(int score) { this.score = score; return this; }
		public DFSResultNode SetCanUse(boolean canUse) { this.canUse = canUse; return this; }
	}
	
	private static final int MAX_DFS_TIME_MILLIS = 100000; // 最大搜索时间
	private static final int[] SEARCH_ARRAY = {7,8,6,9,5,10,4,11,3,12,2,13,1,14};
	private static final int MAX_DFS_LEN = 6; 
	private DFSResultNode DFS(int dep, boolean isMaxNode, int localAlpha, int localBeta)
	{
		if(IsOutOfTime()) 
		{
			return new DFSResultNode().SetScore(0).SetCanUse(false);
		}
//		try {
//			Thread.sleep(0);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		if(dep > MAX_DFS_LEN) 
		{
			int ___ = ScoreEvaluator.Instance().GetBoardScore(!isMaxNode);
//			System.out.println("score -- " + ___);
			return new DFSResultNode().SetScore(___).SetCanUse(true);
		}
		
		// 启发式搜索，按照单点位置的分数对节点进行排序，并从高到低访问
		List<DFSNode> nodeList = new ArrayList<>();
		for(int _1=0;_1<SEARCH_ARRAY.length;_1++) 
		{
			for(int _2=0;_2<SEARCH_ARRAY.length;_2++) 
			{
				int i = SEARCH_ARRAY[_1];
				int j = SEARCH_ARRAY[_2];
				if(m_stCtx.IsEmptyPos(i, j)) 
				{
					nodeList.add(m_arrNodePool.CheckOut().SetX(i).SetY(j).SetScore(ScoreEvaluator.Instance().GetScoreAtPos(i, j, true)));
				}
			}
		}
		nodeList.sort(new Comparator<DFSNode>() {
			@Override
			public int compare(DFSNode o1, DFSNode o2) {
				float dt = o1.score - o2.score;
				if(dt < -0.001f) return 1;
				if(dt > 0.001f) return -1;
				return 0;
			}
		});
		
		
		boolean haveAtLeastOneNodeCanUse = false;
		for (DFSNode node : nodeList) 
		{
			int i = node.x;
			int j = node.y;
			if(!m_stCtx.IsEmptyPos(i, j))
			{
				continue;
			}
			
			if(localAlpha >= localBeta)
			{
				// cut
//				continue;
				break;
			}
			
			boolean _isEmptyAround = true;
			for(int _tx=-1;_tx<=1;_tx++) 
			{
				for(int _ty=-1;_ty<=1;_ty++) 
				{
					if(_tx == 0 && _ty == 0) 
					{
						continue;
					}
					
					if(m_stCtx.IsValidPos(i+_tx, j+_ty) && m_stCtx.IsEmptyPos(i+_tx, j+_ty) == false)
					{
						_isEmptyAround = false;
						break;
					}
				}
			}
			if(_isEmptyAround) 
			{
				continue;
			}
			
			// 尝试在这个位置下棋
			TestSetResult result = m_stCtx.TestSetAt(i, j, isMaxNode);
			if(!result.setSuccess)
			{
				System.out.println("TestSetAt逻辑错误！！");
			}
//			System.out.println("Set at " + i + ".." + j);
			
//			int score = 0;
			DFSResultNode dfsRes = null;
			// 只有游戏还没结束，才继续搜索
			if(result.gameIsEnd == false) 
			{
				// 得到当前局面的缓存分数
//				long boardHashCode = m_stCtx.GetBoardHashCode();
//				PlayerColor nowColor = isMaxNode ? m_stCtx.GetAIColor() : m_stCtx.GetGamerColor();
//				ScoreNode scoreNode = Zobrist.Instance().GetScoreNodeByHashCode(boardHashCode, nowColor);
//				int _boardDep = m_stCtx.GetExistChessCnt() + (MAX_DFS_LEN - dep);
//				if(scoreNode != null && _boardDep <= scoreNode.dep) 
//				{
//					score = scoreNode.score;
//				}
//				else 
//				{
//					score = DFS(dep + 1, !isMaxNode, localAlpha, localBeta);
//					Zobrist.Instance().SetScoreNodeByHashCode(boardHashCode, nowColor, score, _boardDep);
//				}
				
				dfsRes = DFS(dep + 1, !isMaxNode, localAlpha, localBeta);
			}
			else 
			{
				// TODO.. 这里逻辑可能有问题
				dfsRes = new DFSResultNode().SetScore(ScoreEvaluator.Instance().GetBoardScore(!isMaxNode)).SetCanUse(true);
			}
			
			if(dfsRes.canUse) 
			{
				haveAtLeastOneNodeCanUse = true;
				
				int score = dfsRes.score;
				if(isMaxNode) 
				{
					if(score > localAlpha) 
					{
						localAlpha = score;
						
						// 第0层，记录下棋位置
						if(dep == 0)
						{
							setX = i;
							setY = j;
						}
					}
				}
				else 
				{
					if(score < localBeta) 
					{
						localBeta = score;
					}
				}
			}
			
			// 取消这个位置的棋子
			if(!m_stCtx.UnsetAt(i, j))
			{
				System.out.println("UnsetAt逻辑错误！！");
			}
		}
		
		// 归还节点
		for (DFSNode dfsNode : nodeList) 
		{
			m_arrNodePool.CheckIn(dfsNode);
		}
		
//		for(int _1=0;_1<SEARCH_ARRAY.length;_1++) 
//		{
//			for(int _2=0;_2<SEARCH_ARRAY.length;_2++) 
//			{
//				int i = SEARCH_ARRAY[_1];
//				int j = SEARCH_ARRAY[_2];
//				if(!m_stCtx.IsEmptyPos(i, j))
//				{
//					continue;
//				}
//				
//				if(localAlpha >= localBeta)
//				{
//					// cut
//					continue;
//				}
//				
//				boolean _isEmptyAround = true;
//				for(int _tx=-1;_tx<=1;_tx++) 
//				{
//					for(int _ty=-1;_ty<=1;_ty++) 
//					{
//						if(_tx == 0 && _ty == 0) 
//						{
//							continue;
//						}
//						
//						if(m_stCtx.IsValidPos(i+_tx, j+_ty) && m_stCtx.IsEmptyPos(i+_tx, j+_ty) == false)
//						{
//							_isEmptyAround = false;
//							break;
//						}
//					}
//				}
//				if(_isEmptyAround) 
//				{
//					continue;
//				}
//				
//				// 尝试在这个位置下棋
//				if(!m_stCtx.TestSetAt(i, j, isMaxNode))
//				{
//					System.out.println("TestSetAt逻辑错误！！");
//				}
//				
////				System.out.println("Set at " + i + ".." + j);
//				float score = DFS(dep + 1, !isMaxNode, localAlpha, localBeta);
//				if(isMaxNode) 
//				{
//					if(score > localAlpha) 
//					{
//						localAlpha = score;
//						
//						// 第0层，记录下棋位置
//						if(dep == 0)
//						{
//							setX = i;
//							setY = j;
//						}
//					}
//				}
//				else 
//				{
//					if(score < localBeta) 
//					{
//						localBeta = score;
//					}
//				}
//				
//				// 取消这个位置的棋子
//				if(!m_stCtx.UnsetAt(i, j))
//				{
//					System.out.println("UnsetAt逻辑错误！！");
//				}
//			}
//		}
		
		return new DFSResultNode().SetScore(isMaxNode ? localAlpha : localBeta).SetCanUse(haveAtLeastOneNodeCanUse);
	}
	
	private DFSNodePool m_arrNodePool = new DFSNodePool();
	private List<DFSNode> m_arrDFSNodeList = new ArrayList<>();
	private int setX = -1;
	private int setY = -1;
	private int alpha = 0;
	private int beta = 0;
	private void Init()
	{
		alpha = ScoreEvaluator.MIN;
		beta = ScoreEvaluator.MAX;
		setX = setY = -1;
		
		// 记录初始时间
		this.m_lStartTimeMillis = System.currentTimeMillis();
		
		Zobrist.Instance().Clear();
		
//		m_arrDFSNodeList.clear();
//		for(int _1=0;_1<SEARCH_ARRAY.length;_1++) 
//		{
//			for(int _2=0;_2<SEARCH_ARRAY.length;_2++) 
//			{
//				int i = SEARCH_ARRAY[_1];
//				int j = SEARCH_ARRAY[_2];
//				if(m_stCtx.IsEmptyPos(i, j)) 
//				{
//					m_arrDFSNodeList.add(m_arrNodePool.CheckOut().SetX(i).SetY(j).SetScore(ScoreEvaluator.Instance().GetScoreAtPos(i, j, true)));
//				}
//			}
//		}
//		m_arrDFSNodeList.sort(new Comparator<DFSNode>() {
//			@Override
//			public int compare(DFSNode o1, DFSNode o2) {
//				float dt = o1.score - o2.score;
//				if(dt < -0.001f) return 1;
//				if(dt > 0.001f) return -1;
//				return 0;
//			}
//		});
	}
	
	private void AfterAISetChess()
	{
		System.out.println("使用的时间为：" + (System.currentTimeMillis() - m_lStartTimeMillis) / 1000f);
//		for (DFSNode item : m_arrDFSNodeList) 
//		{
//			m_arrNodePool.CheckIn(item);
//		}
//		m_arrDFSNodeList.clear();
	}
	
	/**
	 * 返回是否已经超时
	 * @return
	 */
	private boolean IsOutOfTime() 
	{
		return System.currentTimeMillis() - m_lStartTimeMillis >= MAX_DFS_TIME_MILLIS;
	}
	
	private long m_lStartTimeMillis = 0;
	/**
	 * AI下棋
	 */
	public void DoMove()
	{
		this.Init();
		new AIThread().start();
	}
}
