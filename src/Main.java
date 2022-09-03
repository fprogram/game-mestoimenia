import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class Main extends JFrame {

	public static int FS = 24; // FONT SIZE
	
	private JPanel contentPane;
	
	private static Map<String, String> components;
	private static List<String> groups;
	
	public static Toolkit tk = Toolkit.getDefaultToolkit();
	public static Dimension dm = tk.getScreenSize();
	
	public static int rowHeight = 64;
	
	public static JLabel[] labels;
	public static PlayComponent[] playComponents;
	
	public static final File INI = new File(System.getProperty("user.home")+"\\Desktop\\mest.txt");

	public static void initialize() {
		groups = new LinkedList<>();
		components = new HashMap<>();
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(INI));
			
			FS = Integer.parseInt(br.readLine());
			String inputLine = br.readLine();
			String[] groupsArray = inputLine.split(",");
			for(String groupTitle : groupsArray) {
				groups.add(groupTitle);
			}
			
			while((inputLine = br.readLine()) != null) {
				String[] input = inputLine.split(":");
				addUnit(input[0], input[1]);
			}
			
			rowHeight = (dm.height/2) / groups.size();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Ошибка инициализации: " + e.getMessage());
		}
		
		
	}
	
	private static void addUnit(String title, String group) {
		if(groups.contains(group)) {
			components.put(title, group);
		} else {
			throw new IllegalArgumentException("Несуществующая группа ответа " + group);
		}
	}
	public static void main(String[] args) {
		initialize();
		Main frame = new Main();
		frame.setVisible(true);
	}

	/**
	 * Create the frame.
	 */
	public Main() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(0, 0, dm.width, dm.height);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(null);
		setContentPane(contentPane);
		
		labels = new JLabel[groups.size()];
		for(int i = 0; i < groups.size(); i++) {
			labels[i] = new JLabel(groups.get(i));
			labels[i].setFont(new Font("arial", 0, Math.min(rowHeight - 10, FS)));
			labels[i].setBounds(0, i * rowHeight, dm.width, rowHeight);
			labels[i].setForeground(Color.BLUE);
			labels[i].setBorder(new LineBorder(Color.BLUE, 3));
			add(labels[i]);
		}
		
		int x = 0;
		int y = 0;
		
		playComponents = new PlayComponent[components.size()];
		int i = 0;
		for(Entry<String, String> e : components.entrySet()) {
			playComponents[i] = new PlayComponent(e.getKey(), e.getValue(), x, dm.height/2 + y);
			add(playComponents[i]);
			i++;
			y = y + FS*2;
			if(y >= dm.height/3-FS) {
				y = 0;
				x = x + dm.width/12;
			}
		}
		
		JButton btn = new JButton();
		btn.setText("Проверить!");
		btn.setFont(new Font("arial", 0, Math.min(rowHeight - 10, FS)));
		btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for(PlayComponent pc : playComponents) {
					String rightAnswer = pc.getRightRow();
					int ypos = pc.getY();
					int num = ypos / rowHeight;
					String answer;
					try {
						answer = labels[num].getText();
					} catch(ArrayIndexOutOfBoundsException e2) {
						answer = "wrong!s1";
					}
					
					if(rightAnswer.equals(answer)) {
						pc.setColor(Color.GREEN);
						System.out.println("я поменял цвет " + pc.getTitle() + " на зелёный");
					} else {
						pc.setColor(Color.RED);
					}
					
					pc.repaint();
				}
			}
		});
		btn.setBounds(dm.width-FS*10, dm.height-FS*4, FS * 10, FS);
		add(btn);
		
	}
	
	public static void depaintAll() {
		for(PlayComponent pc : playComponents) {
			pc.setColor(Color.YELLOW);
			pc.repaint();
		}
	}

}

class PlayComponent extends JComponent {

	private volatile int screenX = 0;
	private volatile int screenY = 0;
	private volatile int myX = 0;
	private volatile int myY = 0;
	
	private String title = "...";
	private String rightRow = null;
	private Color cc = Color.YELLOW;
	
	private static final int FS = Main.FS;
	private static final Font f = new Font("arial", 0, FS);

	public PlayComponent(String s, String row, int x, int y) {
		title = s;
		rightRow = row;
		setBorder(new LineBorder(Color.BLUE, 3));
		setBackground(Color.WHITE);
		
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		FontMetrics fm = img.getGraphics().getFontMetrics(f);
		int width = fm.stringWidth(s);
		
		setBounds(x, y, width + 10, (int)(FS * 1.5));
		setOpaque(false);
		myX = getX();
		myY = getY();
		addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) { }
			@Override
			public void mousePressed(MouseEvent e) {
				screenX = e.getXOnScreen();
				screenY = e.getYOnScreen();

				myX = getX();
				myY = getY();
			}

			@Override
			public void mouseReleased(MouseEvent e) { }
		
			@Override
			public void mouseEntered(MouseEvent e) { }
		
			@Override
			public void mouseExited(MouseEvent e) { }
		
			}
		);
		
		addMouseMotionListener(new MouseMotionListener() {

			@Override
		  	public void mouseDragged(MouseEvent e) {
				int deltaX = e.getXOnScreen() - screenX;
		    	int deltaY = e.getYOnScreen() - screenY;
		    	setLocation(myX + deltaX, myY + deltaY);
		    	Main.depaintAll();
		  	}
		
		  	@Override
		  	public void mouseMoved(MouseEvent e) { }

		});
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(cc);
		g.fillRect(0, 0, 1000, 1000);
        g.setColor(Color.BLACK);
        g.setFont(f);
        g.drawString(title, 5, FS);
    }
	
	public String getRightRow() { return rightRow; }
	
	public String getTitle() { return title; }
	
	public void setColor(Color c) { cc = c; }
	
}