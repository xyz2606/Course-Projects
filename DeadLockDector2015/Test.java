import java.awt.*;
import java.awt.event.*;
import java.io.*;

class InFrame extends Frame {
	InFrame() {
		TextArea source = new TextArea();
		source.setSize(200, 200);
		TextArea result = new TextArea();
		result.setEditable(false);
		result.setSize(200, 200);
		Button save = new Button("Save & Compile");
		Button run = new Button("Run!!!");
		Button exit = new Button("Exit");

		result.setText("");
		save.addActionListener(new GUI(source, result));
		run.addActionListener(new CMD(result));
		exit.addActionListener(new EXIT());
		setLayout(new FlowLayout());
		add(source);
		add(save);
		add(run);
		add(exit);
		add(result);
		pack();
		setVisible(true);
	}
}

class EXIT implements ActionListener {
	public EXIT() {
	}

	public void actionPerformed(ActionEvent e) {
		System.exit(0);
	}
}

class GUI implements ActionListener {
	TextArea source, result;
	public GUI(TextArea source, TextArea result) {
		this.source = source;
		this.result = result;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			result.setText("Saving...\n");
			File file = new File("source.c");
			if (!file.exists()) {
				file.createNewFile();
			}
			FileOutputStream out = new FileOutputStream("source.c");
			PrintStream p = new PrintStream(out);
			p.println(source.getText());
			out.close();
			result.append("Compling...\n");
			Process pro;
			pro = Runtime.getRuntime().exec("gcc -fPIC -shared -o hook.so hook.c -ldl");
			pro.waitFor();
			pro = Runtime.getRuntime().exec("gcc -o source source.c -lpthread");
			pro.waitFor();
		} catch(Exception ee) {
			System.out.println("Compile Error");
		}
	}
}

class CMD implements ActionListener {
	TextArea result;
	public CMD(TextArea result) {
		this.result = result;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			result.setText("");

			File commandFile = new File("fuck.sh");
			commandFile.createNewFile();
			{
				PrintStream out = new PrintStream(new FileOutputStream("fuck.sh"));
				out.println("LD_PRELOAD=./hook.so ./source");
			}
			String cmd = "./fuck.sh";
			Process pro = Runtime.getRuntime().exec(cmd);
			//commandFile.delete();
			pro.waitFor();

			LineNumberReader line = new LineNumberReader(new InputStreamReader(pro.getInputStream()));
			String lineStr, res = new String("");

			while ((lineStr = line.readLine()) != null) {
	   			res = res + lineStr;
	  			res = res + "\n";
	 		}
			if (pro.exitValue() == 1) {
				result.append("Runtime Error --- c");
			} else {
				result.append("------- standard output -------\n");
		 		result.append(res);
				result.append("----------------------------\n");
				result.append("\n");

				File reportFile = new File("report.txt");
				if (!reportFile.exists()) {
					reportFile.createNewFile();
				}
				BufferedReader reader = new BufferedReader(new FileReader(reportFile));
				String tempString = null, reportString = null;
				int lineCnt = 0;
				while ((tempString = reader.readLine()) != null) {
					reportString = reportString + tempString;
					reportString = reportString + '\n';
					lineCnt++;
				}
				result.append("------- report output ------\n");
				if (lineCnt > 0) {
					result.append(reportString);
				} else {
					result.append("No DeadLock Found\n");
				}
				result.append("--------------------------\n");

			}

		} catch(Exception ee) {
			System.out.println("Runtime Error --- java");
		}
	}
}

public class Test {
	public static void main(String args[]) {
		new InFrame();
	}
}
