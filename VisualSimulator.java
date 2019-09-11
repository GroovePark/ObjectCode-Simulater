package sp18_simulator;

import java.awt.EventQueue;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;



/**
 * VisualSimulator는 사용자와의 상호작용을 담당한다.<br>
 * 즉, 버튼 클릭등의 이벤트를 전달하고 그에 따른 결과값을 화면에 업데이트 하는 역할을 수행한다.<br>
 * 실제적인 작업은 SicSimulator에서 수행하도록 구현한다.
 */
public class VisualSimulator {
	ResourceManager resourceManager = new ResourceManager();
	SicLoader sicLoader = new SicLoader(resourceManager);
	SicSimulator sicSimulator = new SicSimulator(resourceManager);
	
	
	
	
	///////////GUI 콘솔창을 선언하는 부분 ////////////////////////////////////////////////
	///////////////////////////////////////
	JFrame frame = new JFrame("simulator");
	JButton openbutton = new JButton("open");
	JMenuBar menuBar = new JMenuBar();
	JMenu fileMenu = new JMenu("파일");
	JMenu helpMenu = new JMenu("Help");
	JPanel pane = new JPanel();
	JTextField filenametext = new JTextField(10);
	JLabel filenamelabel = new JLabel("File Name : ");
	//////////////////////////////////////////////////
	
	//////////////////////////////////////////////////
	JLabel h1 = new JLabel("H (Header Record)");
	JLabel h2 = new JLabel("Program Name :");
	JLabel h3 = new JLabel("Start Address of");  
	JLabel h4 = new JLabel("  Object Program :");
	JLabel h5 = new JLabel("Length of Program :");
	JTextField t1 = new JTextField(7);
	JTextField t2 = new JTextField(6);
	JTextField t3 = new JTextField(6);
	///////////////////////////////////////////////////
	
	JLabel r1 = new JLabel("Register");
	JLabel r2 = new JLabel("Dec               Hex");
	JLabel r3 = new JLabel("A(#0)");  
	JLabel r4 = new JLabel("X(#1)");
	JLabel r5 = new JLabel("L(#2)");
	JLabel r6 = new JLabel("PC(#8)");
	JLabel r7 = new JLabel("SW(#9)");
	JTextField rt1 = new JTextField(6); JTextField rt2 = new JTextField(6);
	JTextField rt3 = new JTextField(6); JTextField rt4 = new JTextField(6);
	JTextField rt5 = new JTextField(6); JTextField rt6 = new JTextField(6);
	JTextField rt7 = new JTextField(6); JTextField rt8 = new JTextField(6);
	JTextField rt9 = new JTextField(6);
	//////////////////////////////////////////////////////
	
	JLabel rx1 = new JLabel("Register(for XE)");
	JLabel rx2 = new JLabel("Dec               Hex");
	JLabel rx3 = new JLabel("B(#3)");  
	JLabel rx4 = new JLabel("S(#4)");
	JLabel rx5 = new JLabel("T(#5)");
	JLabel rx6 = new JLabel("F(#6)");
	JTextField rxt1 = new JTextField(6); JTextField rxt2 = new JTextField(6);
	JTextField rxt3 = new JTextField(6); JTextField rxt4 = new JTextField(6);
	JTextField rxt5 = new JTextField(6); JTextField rxt6 = new JTextField(6);
	JTextField rxt7 = new JTextField(6);
	///////////////////////////////////////////////////////
	
	JLabel e1 = new JLabel("E (End Record)");
	JLabel e2 = new JLabel("Address of First Instruction");
	JLabel e3 = new JLabel("in Object Program :");
	JTextField et1 = new JTextField(6); 
	///////////////////////////////////////////////////////
	
	JLabel s1 = new JLabel("Start Address in Memory");
	JLabel s2 = new JLabel("TargetAddress :");
	JLabel s3 = new JLabel("Instructions :");
	JTextField st1 = new JTextField(6);
	JTextField st2 = new JTextField(6);
	TextArea st3 = new TextArea(10, 10);
	JButton sb1 = new JButton("1 Step");
	JButton sb2 = new JButton("ALL");
	JButton sb3 = new JButton("EXIT");
	//////////////////////////////////////////////////////
	JLabel log = new JLabel("Simulated MEMORY");
	JButton memory = new JButton("update Memory");
	TextArea logt = new TextArea(50,50);
	
	//GUI 인터페이스 생성 함수
	public void createGUI()
	{	
		fileMenu.add(new JMenuItem("열기"));
		menuBar.add(fileMenu);
		////헤더래코드 표시위치//////////
		h1.setBounds(10,30,150,30);
		h2.setBounds(25,50,150,30);
		h3.setBounds(25,70,150,30);
		h4.setBounds(25,85,150,30);
		h5.setBounds(20,105,180,30);
		t1.setBounds(130,56,100,20);
		t2.setBounds(135,88,100,20);
		t3.setBounds(135,110,100,20);
	
		frame.add(h1);frame.add(h2);frame.add(h3);
		frame.add(h4);frame.add(h5);
		frame.add(t1);frame.add(t2);frame.add(t3);
		
		///////SIC레지스터 표시위치//////////
		r1.setBounds(10,140,150,30);
		r2.setBounds(80,160,150,30);
		r3.setBounds(25,180,150,30);
		r4.setBounds(25,200,150,30);
		r5.setBounds(25,220,180,30);
		r6.setBounds(17,240,180,30);
		r7.setBounds(17,260,180,30);
		rt1.setBounds(80,187,50,20); rt2.setBounds(150,187,50,20);
		rt3.setBounds(80,207,50,20); rt4.setBounds(150,207,50,20);
		rt5.setBounds(80,227,50,20); rt6.setBounds(150,227,50,20);
		rt7.setBounds(80,247,50,20); rt8.setBounds(150,247,50,20);
		rt9.setBounds(80,267,90,20);
		
		frame.add(r1);frame.add(r2);frame.add(r3);frame.add(r4);
		frame.add(r5);frame.add(r6);frame.add(r7);
		frame.add(rt1);frame.add(rt2);
		frame.add(rt3);frame.add(rt4);
		frame.add(rt5);frame.add(rt6);
		frame.add(rt7);frame.add(rt8);
		frame.add(rt9);
		//////////////////////////////////////////////////////
		
		///////SIC(For XE)레지스터 표시위치///////////////////////
		rx1.setBounds(10,290,150,30);
		rx2.setBounds(80,310,150,30);
		rx3.setBounds(25,330,150,30);
		rx4.setBounds(25,350,150,30);
		rx5.setBounds(25,370,150,30);
		rx6.setBounds(25,390,150,30);
		rxt1.setBounds(80,337,50,20); rxt2.setBounds(150,337,50,20);
		rxt3.setBounds(80,357,50,20); rxt4.setBounds(150,357,50,20);
		rxt5.setBounds(80,377,50,20); rxt6.setBounds(150,377,50,20);
		rxt7.setBounds(80,397,90,20);
		
		frame.add(rx1);frame.add(rx2);frame.add(rx3);frame.add(rx4);
		frame.add(rx5);frame.add(rx6);
		frame.add(rxt1);frame.add(rxt2);
		frame.add(rxt3);frame.add(rxt4);
		frame.add(rxt5);frame.add(rxt6);
		frame.add(rxt7);
		//////////////////////////////////////////////////////
		
		/////////End 레코드 표시위치///////////////////////////////
		e1.setBounds(300,35,140,20);
		e2.setBounds(310,55,200,20);
		e3.setBounds(315,72,150,20);
		et1.setBounds(430,72,80,20);
		frame.add(e1); frame.add(e2); frame.add(e3); frame.add(et1);
		///////////////////////////////////////////////////////
		
		////////INSTRUCTION 목록///////////////////////////////
		s1.setBounds(295,110,140,20);
		s2.setBounds(295,150,100,20);
		s3.setBounds(295,170,90,20);
		st1.setBounds(390,130,100,20);
		st2.setBounds(390,150,100,20);
		st3.setBounds(290,190,100,240);
		sb1.setBounds(400, 320, 100, 25);
		sb2.setBounds(400, 360, 100, 25);
		sb3.setBounds(400, 400, 100, 25);
		
		frame.add(s1); frame.add(s2); frame.add(s3);
		frame.add(st1);frame.add(st2);frame.add(st3);
		frame.add(sb1);frame.add(sb2);frame.add(sb3);
		////////////////////////////////////////////////////////
		
		log.setBounds(20,440,150,20);
		memory.setBounds(200, 440, 150, 20);
		logt.setBounds(15,460,500,150);
		frame.add(log);frame.add(logt);
		frame.add(memory);
		
		
		pane.add(filenamelabel);
		pane.add(filenametext);
		pane.add(openbutton);
		frame.add(pane);
		frame.setJMenuBar(menuBar);
		frame.setSize(550, 700);
		frame.setVisible(true);
		
		
		///파일종료버튼 /////////
		sb3.addActionListener
		(
			new ActionListener() 
			{
				public void actionPerformed(ActionEvent e) 
				{
				System.exit(0);
				}
			}
		);
		
		//파일 오픈 기능 버튼 //////////////////////
		openbutton.addActionListener
		(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent e) 
					{
					JFileChooser open = new JFileChooser();
					open.setDialogTitle("file open");
					FileNameExtensionFilter filter  = new FileNameExtensionFilter("txt & dat","txt","dat");
					open.setFileFilter(filter);
					int ret = open.showOpenDialog(null);
					if(ret != JFileChooser.APPROVE_OPTION) {
						JOptionPane.showMessageDialog(null,"not choose file","warning",JOptionPane.WARNING_MESSAGE);
						return ;                      
						}
					else 
						{
						
						String filename = open.getSelectedFile().getName();
						filenametext.setText(filename);
						try {
							load(open.getSelectedFile());
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						}
					}
				}
		);
		
		////// one step button 기능 ////////////
		sb1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
					oneStep();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }
        });
		
		// all stemp 버튼 기능 //////////////////////////
		sb2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
					allStep();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }
        });
		
		//// memory 새로고침 버튼 기능 ////////////////////////
		memory.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                update_memory();
            }
        });

	}
	
	/*memory를 update하는 함수이다.  
	버튼이 클릭 되었을 때 simulated memory값을 최신버전으로 새로고침한다.*/
	public void update_memory()
	{	
		int i,j,k;
		String s = null;
		logt.setText("");
		j = 0;
		int space = 0;
		logt.append("MEMORY\n");
		logt.append("ADDRESS                Contents\n");
		logt.append("---------   ---------------------------------------");
		for(i=0;i<7;i++)
		{	
			s = String.format("%04X", j);
			logt.append("\n  " + s + "    :    ");
			for(k = 0;k<16;k++,j++)
			{	
				
				 s = String.format("%01X", resourceManager.memory[j]>>4);
				logt.append(s);
				 s = String.format("%01X", resourceManager.memory[j]&15);
				logt.append(s);
				space++;
				if(space==4)
				{
					logt.append(" ");
					space =0;
				}
			}
		}
		logt.append("\n    .\n");
		logt.append("    .\n");
		logt.append("    .\n");
		j = 4096;
		for(i=0;i<9;i++)
		{	
			s = String.format("%04X", j);
			logt.append("\n  " + s + "   :   ");
			for(k = 0;k<16;k++,j++)
			{	
				 s = String.format("%01X", resourceManager.memory[j]>>4);
				logt.append(s);
				 s = String.format("%01X", resourceManager.memory[j]&15);
				logt.append(s);
				space++;
				if(space==4)
				{
					logt.append(" ");
					space =0;
				}
			}
		}
	}
	
	
	/**
	 * 프로그램 로드 명령을 전달한다.
	 * @throws IOException 
	 */
	public void load(File program) throws IOException{
		//...
		int i,j,k;
		String s = null;
		// 시작주소값, 프로그램 이름과 길이 등등을 업데이트 해주고 simulated memory를 업뎃해준다.
		sicSimulator.load(program);
		sicLoader.load(program);
		t1.setText(resourceManager.programname);
		t2.setText(resourceManager.startaddress);
		t3.setText(String.format("%02X", resourceManager.program_length));
		et1.setText(resourceManager.startaddress);
		st1.setText(resourceManager.startaddress);
		System.out.println(resourceManager.symtabList.symbolList);
		System.out.println(resourceManager.symtabList.addressList);
		j = 0;
		int space = 0;
		logt.append("MEMORY\n");
		logt.append("ADDRESS                Contents\n");
		logt.append("---------   ---------------------------------------");
		for(i=0;i<7;i++)
		{	
			s = String.format("%04X", j);
			logt.append("\n  " + s + "    :    ");
			for(k = 0;k<16;k++,j++)
			{	
				
				 s = String.format("%01X", resourceManager.memory[j]>>4);
				logt.append(s);
				 s = String.format("%01X", resourceManager.memory[j]&15);
				logt.append(s);
				space++;
				if(space==4)
				{
					logt.append(" ");
					space =0;
				}
			}
		}
		logt.append("\n    .\n");
		logt.append("    .\n");
		logt.append("    .\n");
		j = 4096;
		for(i=0;i<9;i++)
		{	
			s = String.format("%04X", j);
			logt.append("\n  " + s + "   :   ");
			for(k = 0;k<16;k++,j++)
			{	
				 s = String.format("%01X", resourceManager.memory[j]>>4);
				logt.append(s);
				 s = String.format("%01X", resourceManager.memory[j]&15);
				logt.append(s);
				space++;
				if(space==4)
				{
					logt.append(" ");
					space =0;
				}
			}
		}
		
		//레지스터 값들을 업데이트 해주는 함수
		update();
		
	};

	/**
	 * 하나의 명령어만 수행할 것을 SicSimulator에 요청한다.
	 * @throws IOException 
	 */
	public void oneStep() throws IOException{
		
		st3.append(sicSimulator.oneStep()+"\n");
		if(resourceManager.nowloc == Integer.parseInt(resourceManager.startaddress,16))
		{
			resourceManager.closeDevice();
			JOptionPane.showMessageDialog(null, "Process was Finished");
		}
		update();
	};

	/**
	 * 남아있는 모든 명령어를 수행할 것을 SicSimulator에 요청한다.
	 * @throws IOException 
	 */
	public void allStep() throws IOException{
		oneStep();
		while(resourceManager.nowloc != Integer.parseInt(resourceManager.startaddress,16))
			oneStep();
	};
	
	/**
	 * 화면을 최신값으로 갱신하는 역할을 수행한다.
	 */
	public void update(){
		rt1.setText(String.format("%d",resourceManager.register[0]));
		rt2.setText(String.format("%04X",resourceManager.register[0]));
		rt3.setText(String.format("%d",resourceManager.register[1]));
		rt4.setText(String.format("%04X",resourceManager.register[1]));
		rt5.setText(String.format("%d",resourceManager.register[2]));
		rt6.setText(String.format("%04X",resourceManager.register[2]));
		rt7.setText(String.format("%d",resourceManager.register[8]));
		rt8.setText(String.format("%04X",resourceManager.register[8]));
		rt9.setText(String.format("%d",resourceManager.register[9]));
		
		
		rxt1.setText(String.format("%d",resourceManager.register[3]));
		rxt2.setText(String.format("%04X",resourceManager.register[3]));
		rxt3.setText(String.format("%d",resourceManager.register[4]));
		rxt4.setText(String.format("%04X",resourceManager.register[4]));
		rxt5.setText(String.format("%d",resourceManager.register[5]));
		rxt6.setText(String.format("%04X",resourceManager.register[5]));
		rxt7.setText(String.format("%d",resourceManager.register[6]));
		
		st2.setText(String.format("%04X",resourceManager.nowloc));
		
	};
	

	public static void main(String[] args) {
	VisualSimulator simulator = new VisualSimulator();	
	simulator.createGUI();
	
	
	}
}
