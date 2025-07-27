package pers.di.common;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

public class CEventSys {

	private static Map<String, Method> m_EventCreateMap = new HashMap<String, Method>();
	private static String s_point = "inproc://base_event";
	private static String s_QuitCmdPrefix = "EVENTSYS_QUIT_";
	private static Context s_context;
	private static List<EventReceiver> s_receiverList = new ArrayList<EventReceiver>();
	
	public static boolean start()
	{
		CLog.debug("COMMON", "BL_EventSys Initialized...\n");
		try {
			// init context
			s_context = ZMQ.context(1);
			// init pub
			EventSender.initialize();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	
	public static boolean stop()
	{
		CLog.debug("COMMON", "BL_EventSys UnInitialize...\n");

		// stop all receiver
		for(int i=0; i< CEventSys.s_receiverList.size(); i++)
		{
			EventReceiver cReceiver = CEventSys.s_receiverList.get(i);
			cReceiver.stopReceive();
		}
		// unInit pub
		EventSender.unInitialize();
		s_context.term();
		return true;
	}

	/*
	 * Sender
	 */
	public static class EventSender
	{
		private static Socket s_PubSocket;
		private static CSyncObj s_PubSync; 
		
		public static boolean initialize()
		{
			s_PubSocket = s_context.socket(ZMQ.PUB);
			s_PubSocket.setLinger(5000);
			s_PubSocket.setSndHWM(0);
			s_PubSocket.bind(s_point);
			s_PubSync = new CSyncObj();
			return true;
		}
		public static boolean unInitialize()
		{
			s_PubSocket.close();
			return true;
		}
		
		public boolean Send(String name, JSONObject jsonObj)
		{
			s_PubSync.Lock();
			if(null == jsonObj) 
			{
				CLog.debug("COMMON", "Sender EvName(%s) jsonObj(null)\n", name);
				s_PubSocket.send(name.getBytes(), ZMQ.SNDMORE); 
				s_PubSocket.send("null".getBytes(), 0);
			}
			else
			{
				CLog.debug("COMMON", "Sender EvName(%s) Data(...)\n", name);
				s_PubSocket.send(name.getBytes(), ZMQ.SNDMORE); 
				s_PubSocket.send(jsonObj.toString().getBytes(), 0);
			}
			s_PubSync.UnLock();
			return true;
		}
	}
	
	/*
	 * Receiver
	 */
	public static class EventReceiver
	{
		public static interface EventReceiverCB {
			void callback(JSONObject jsonObj);
		}
		
		private static class ReceiverThread extends Thread
		{
			public ReceiverThread(EventReceiver receiver)
			{
				m_receiver = receiver;
			}
			@Override
	        public void run()
	        {
				CLog.debug("COMMON", "EventReceiver(%s) thread running.\n",  m_receiver.m_ReceiverName);
				while(!m_receiver.m_Quit){
					
					m_receiver.m_SubSync.Lock();
					
					byte[] nameBytes = m_receiver.m_SubSocket.recv(0);
					String name = new String(nameBytes);
					
					byte[] jsonObjBytes = m_receiver.m_SubSocket.recv(0);
					String jsonObjStr = new String(jsonObjBytes);
					
					CLog.debug("COMMON", "Receiver(%s) EvName(%s) jsonObj(...)\n", m_receiver.m_ReceiverName, name);
					
					// 退出命令
					if((s_QuitCmdPrefix+m_receiver.m_ReceiverName).compareTo(name) == 0)
					{
						break;
					}
					
					// 用户回调函数
					FuncObj funcObj = m_receiver.m_cbMap.get(name);
					
					// 构造事件实例
					JSONObject jObject = null;
					if(jsonObjStr.compareTo("null") != 0)
					{
						jObject = new JSONObject(jsonObjStr);
					}

					m_receiver.m_SubSync.UnLock();
					
					// 回到用户函数
					try {
						funcObj.m.invoke(funcObj.o, jObject);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}
				m_receiver.m_SubSocket.close();
				CLog.debug("COMMON", "EventReceiver(%s) thread exit!\n", m_receiver.m_ReceiverName);
	        }
			public EventReceiver m_receiver;
		}
		
		private ReceiverThread m_receiverThread;
		private String m_ReceiverName;
		private Socket m_SubSocket;
		private CSyncObj m_SubSync; 
		private static class FuncObj
		{
			public FuncObj(Object obj, Method md)
			{
				o = obj;
				m = md;
			}
			public Object o;
			public Method m;
		}
		private Map<String, FuncObj> m_cbMap;
		private boolean m_Quit;
		
		public EventReceiver(String name)
		{
			m_receiverThread = new ReceiverThread(this);
			m_Quit = false;
			m_ReceiverName = name;
			Context context = CEventSys.s_context;
			m_SubSocket = context.socket(ZMQ.SUB);
			m_SubSocket.connect(s_point);
			m_SubSync = new CSyncObj();
			m_cbMap = new HashMap<String, FuncObj>();
			Subscribe(s_QuitCmdPrefix+m_ReceiverName, null, null);
			// add list
			CEventSys.s_receiverList.add(this);
		}
		
		public boolean Subscribe(String name, Object obj, String methodname)
		{
			try {
				if(null != obj && null!= methodname)
				{
					Class<?> clz = Class.forName(obj.getClass().getName());
					Method md = clz.getMethod(methodname, JSONObject.class);
					m_cbMap.put(name, new FuncObj(obj, md));
				}
				m_SubSocket.subscribe(name.getBytes());
				CLog.debug("COMMON", "%s Subscribe %s\n", m_ReceiverName, name);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}
		
		public boolean startReceive()
		{
			CLog.debug("COMMON", "EventReceiver(%s) startReceive\n", m_ReceiverName);
			m_receiverThread.start();
			return true;
		}
		
		public boolean stopReceive()
		{
			CLog.debug("COMMON", "EventReceiver(%s) stopReceive\n", m_ReceiverName);
			EventSender cSender = new EventSender();
			cSender.Send(s_QuitCmdPrefix+m_ReceiverName, null);
			try {
				m_receiverThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return true;
		}
		
		public String getName()
		{
			return m_ReceiverName;
		}
	}
}
