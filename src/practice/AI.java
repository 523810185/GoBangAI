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
			
			// ������������
//			for(int dfsLen = 2; dfsLen <= MAX_DFS_LEN; dfsLen += 2)
//			{
//				if(IsOutOfTime()) 
//				{
//					break;
//				}
//				m_iDFSLowestDep = MAX_DFS_LEN - dfsLen;
//				alpha = DFS(m_iDFSLowestDep, true, alpha, beta).score;
//			}
			
//			m_iDFSLowestDep = 0;
//			DFS(m_iDFSLowestDep, true, alpha, beta);
			
//			MTDF(alpha, beta, 0, 0);
			
			alpha_beta(alpha, beta, 0, true);
			
			AfterAISetChess();
			if(!m_stCtx.IsEmptyPos(setX, setY))
			{
				System.out.println("AI DFS �߼�����" + setX + " " + setY);
			}
			else 
			{
				m_stCtx.AIPutAt(setX, setY);
			}
		}
	}
	
	private int m_iDFSLowestDep = -1;
	
	/**
	 * ����ʽ������Ԥ����ڵ�
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
		public boolean canUse = false; // �Ƿ����
		public DFSResultNode SetScore(int score) { this.score = score; return this; }
		public DFSResultNode SetCanUse(boolean canUse) { this.canUse = canUse; return this; }
	}
	
	private static final int MAX_DFS_TIME_MILLIS = 10000; // �������ʱ��
	private static final int[] SEARCH_ARRAY = {7,8,6,9,5,10,4,11,3,12,2,13,1,14};
	private static final int MAX_DFS_LEN = 5; 
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
		
		// ����ʽ���������յ���λ�õķ����Խڵ�������򣬲��Ӹߵ��ͷ���
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
			
			// ���������λ������
			TestSetResult testSetResult = m_stCtx.TestSetAt(i, j, isMaxNode);
			if(!testSetResult.setSuccess)
			{
				System.out.println("TestSetAt�߼����󣡣�");
			}
//			System.out.println("Set at " + i + ".." + j);
			
//			int score = 0;
			DFSResultNode dfsRes = null;
			// ֻ����Ϸ��û�������ż�������
			if(testSetResult.gameIsEnd == false) 
			{
				// �õ���ǰ����Ļ������
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
				
				// pvs 
//				if(DFS(dep + 1, !isMaxNode, localAlpha, localAlpha + 1).score <= localAlpha)
//				{
//					// ȡ�����λ�õ�����
//					if(!m_stCtx.UnsetAt(i, j))
//					{
//						System.out.println("UnsetAt�߼����󣡣�");
//					}
//					break;
//				}
				
				dfsRes = DFS(dep + 1, !isMaxNode, localAlpha, localBeta);
			}
			else 
			{
				// TODO.. �����߼�����������
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
						
						// ��0�㣬��¼����λ��
						if(dep == m_iDFSLowestDep)
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
			
			// ȡ�����λ�õ�����
			if(!m_stCtx.UnsetAt(i, j))
			{
				System.out.println("UnsetAt�߼����󣡣�");
			}
		}
		
		// �黹�ڵ�
		for (DFSNode dfsNode : nodeList) 
		{
			m_arrNodePool.CheckIn(dfsNode);
		}
		
		return new DFSResultNode().SetScore(isMaxNode ? localAlpha : localBeta).SetCanUse(haveAtLeastOneNodeCanUse);
	}
	
	private int MTDF(int localAlpha, int localBeta, int dep, int test) 
	{
//		int best_value;
//		do 
//		{
//			// �����������̽
//			best_value = alpha_beta(test-1, test, dep, true);
//			// �����alpha�ڵ�
//			if (best_value < test) {
//				// ���¹�ֵ���ޣ���������Ϊ�µ���ֵ̽
//				test = localBeta = best_value;
//			// ������beta�ڵ�
//			} else {
//				// ���¹�ֵ����
//				localAlpha = best_value;
//				// �µ���ֵ̽
//				test = best_value + 1;
//			}
//		} while (localAlpha < localBeta);
//		return best_value;
		
		int g = test;
		int _beta;
		while(localAlpha < localBeta)
		{
			if(g == localAlpha) 
			{
				_beta = g + 1;
			}
			else 
			{
				_beta = g;
			}
			
			g = alpha_beta(_beta - 1, _beta, 0, true);
			
			if(g < _beta) 
			{
				localBeta = g;
			}
			else 
			{
				localAlpha = g;
			}
		}
		return g;
	}
	
	private int alpha_beta(int localAlpha, int localBeta, int dep, boolean isAI){
		// ��ǰ��ѷ�ֵ��Ԥ��Ϊ�������
		int best_value = ScoreEvaluator.MIN;
		// ����ʽ����
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
		
		for (DFSNode node : nodeList) 
		{
			int i = node.x;
			int j = node.y;
			if(!m_stCtx.IsEmptyPos(i, j))
			{
				continue;
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
			
			// ���������λ������
			TestSetResult testSetResult = m_stCtx.TestSetAt(i, j, isAI);
			if(!testSetResult.setSuccess)
			{
				System.out.println("TestSetAt�߼����󣡣�");
			}
			
			int score = 0;
			// ֻ����Ϸ��û�������ż�������
			if(testSetResult.gameIsEnd == false) 
			{
				// �õ���ǰ����Ļ������
//				long boardHashCode = m_stCtx.GetBoardHashCode();
//				PlayerColor nowColor = isAI ? m_stCtx.GetAIColor() : m_stCtx.GetGamerColor();
//				ScoreNode scoreNode = Zobrist.Instance().GetScoreNodeByHashCode(boardHashCode, nowColor);
//				int _boardDep = m_stCtx.GetExistChessCnt() + (MAX_DFS_LEN - dep);
//				if(scoreNode != null && _boardDep <= scoreNode.dep) 
//				{
//					score = scoreNode.score;
//				}
//				else 
//				{
//					if(dep > MAX_DFS_LEN) 
//					{
//						score = -ScoreEvaluator.Instance().GetBoardScore(isAI);
//					}
//					else 
//					{
//						score = -alpha_beta(-localBeta, -localAlpha, dep + 1, !isAI);
//					}
//					Zobrist.Instance().SetScoreNodeByHashCode(boardHashCode, nowColor, score, _boardDep);
//				}
				
				if(dep >= MAX_DFS_LEN) 
				{
					score = -ScoreEvaluator.Instance().GetBoardScore(isAI);
				}
				else 
				{
//					score = -alpha_beta(-localBeta, -localAlpha, dep + 1, !isAI);
					
					// pvs
					score = -alpha_beta(-localAlpha-1, -localAlpha, dep + 1, !isAI);
					if(score > localAlpha) 
					{
						localAlpha = score;
						if(score < localBeta) 
						{
							// ���ᱻ��֦
							score = -alpha_beta(-localBeta, -localAlpha, dep + 1, !isAI);
						} 
					}
				}
			}
			else 
			{
				// TODO.. �����߼�����������
				score = ScoreEvaluator.Instance().GetBoardScore(isAI) * (isAI ? 1 : -1);
			}
			
			// ȡ�����λ�õ�����
			if(!m_stCtx.UnsetAt(i, j))
			{
				System.out.println("UnsetAt�߼����󣡣�");
			}
			
			if(score >= localBeta) 
			{
				// cut
				return score;
			}
			if(score > best_value) 
			{
				best_value = score;
				if(dep == 0)
				{
					setX = i;
					setY = j;
				}
				
				if(score > localAlpha) 
				{
					localAlpha = score;
				}
			}
		}
		
		// �黹�ڵ�
		for (DFSNode dfsNode : nodeList) 
		{
			m_arrNodePool.CheckIn(dfsNode);
		}
		
		// ������ѽ��
		return best_value;
	}
	
	private DFSNodePool m_arrNodePool = new DFSNodePool();
	private int setX = -1;
	private int setY = -1;
	private int alpha = 0;
	private int beta = 0;
	private void Init()
	{
		alpha = ScoreEvaluator.MIN;
		beta = ScoreEvaluator.MAX;
		setX = setY = -1;
		
		// ��¼��ʼʱ��
		this.m_lStartTimeMillis = System.currentTimeMillis();
		
		Zobrist.Instance().Clear();
	}
	
	private void AfterAISetChess()
	{
		System.out.println("ʹ�õ�ʱ��Ϊ��" + (System.currentTimeMillis() - m_lStartTimeMillis) / 1000f);
	}
	
	/**
	 * �����Ƿ��Ѿ���ʱ
	 * @return
	 */
	private boolean IsOutOfTime() 
	{
		return System.currentTimeMillis() - m_lStartTimeMillis >= MAX_DFS_TIME_MILLIS;
	}
	
	private long m_lStartTimeMillis = 0;
	/**
	 * AI����
	 */
	public void DoMove()
	{
		this.Init();
		new AIThread().start();
	}
}
