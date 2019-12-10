/*
 * Copyright (c) 2004, Steven Baldasty <sbaldasty@bitflippin.org>
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * Contributors:
 *    Steven Baldasty <sbaldasty@bitflippin.org>
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 *
 */
package org.bitflippin.wumpus;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

public class Wumpus extends JApplet implements ActionListener, KeyListener  {

	// Dimensions of map in Cells
	public static final int WIDTH = 10;
	public static final int HEIGHT = 7;

	// Messages displayed in the title label
	// Welcome message or outcome of previous game
	public static final String MSG_DEFAULT = "Setup Screen";
	public static final String MSG_EATEN = "You got eaten by the Wumpus!";
	public static final String MSG_SHOT = "Yay! You shot the Wumpus!";
	public static final String MSG_FALLEN = "You fell into a slime pit!";

	// Show outcome of last game in title
	public void message(String s)  { ctl_title.setText(s); }

	// Group of cells that form map
	// Cells use get method to access neighbors
	private Cell cells[][] = new Cell[WIDTH][HEIGHT];
	public Cell getCell(int x, int y)  { return cells[x][y]; }

	// Cell chosen for wumpus
	// Used to show where wumpus is if missed
	private Room wumpus;

	// Cell currently occupied by hunter
	// Null implies dead or setting up
	private Cell hunter;
	public void setHunter(Cell c)  { hunter = c; }

	// Redraw visible part of board if playing game
	// Otherwise repaint controls
	public void paint(Graphics g)  {
		if(ctl_setup.isVisible())
			ctl_setup.paint(g);
		else  {
			for(int x = 0; x < WIDTH; x++)
				for(int y = 0; y < HEIGHT; y++)
					cells[x][y].draw(graphics);
			g.drawImage(buffer, 0, 0, this);
		}
	}

	// Attempt to generate map with given parms
	// Return true on success, false on failure
	// Let t be probability of creating tunnel set
	public boolean createMap(int p, double t)  {
		for(int x = 0; x < WIDTH; x++)
			for(int y = 0; y < HEIGHT; y++)
				if(Math.random() > t)
					cells[x][y] = new Room(x, y);
				else
					cells[x][y] = new Tunnel(x, y);
		for(int i = 0; i < p; i++)
			if(!placePit()) return false;
		if(!placeWumpus()) return false;
		if(!placeHunter()) return false;
		return true;
	}

	// Choose suitable random room and place pit there
	// True on success, false on failure
	public boolean placePit()  {
		LinkedList l = new LinkedList();
		for(int x = 0; x < WIDTH; x++)
			for(int y = 0; y < HEIGHT; y++)  {
				Cell c = cells[x][y];
				if(c.isRoom())  {
					Room r = (Room)(c);
					if(!r.hasPit()) l.add(r);
				}
			}
		int s = l.size();
		if(s > 0)  {
			int i = (int)(Math.random() * s);
			Room f = (Room)(l.get(i));
			f.addPit();
			return true;
		}
		return false;
	}

	// Place wumpus in random room
	// True on success, false on failure
	public boolean placeWumpus()  {
		LinkedList l = new LinkedList();
		for(int x = 0; x < WIDTH; x++)
			for(int y = 0; y < HEIGHT; y++)  {
				Cell c = cells[x][y];
				if(c.isRoom())  {
					Room r = (Room)(c);
					l.add(r);
				}
			}
		int s = l.size();
		if(s > 0)  {
			int i = (int)(Math.random() * s);
			wumpus = (Room)(l.get(i));
			wumpus.addWumpus();
			return true;
		}
		return false;
	}

	// Place hunter in random room
	// True on success, false on failure
	public boolean placeHunter()  {
		LinkedList l = new LinkedList();
		for(int x = 0; x < WIDTH; x++)
			for(int y = 0; y < HEIGHT; y++)  {
				Cell c = cells[x][y];
				if(c.isRoom())  {
					Room r = (Room)(c);
					if(!r.hasPit() && !r.hasWumpus() && !r.hasSwamp() && !r.hasLair()) l.add(r);
				}
			}
		int s = l.size();
		if(s > 0)  {
			int i = (int)(Math.random() * s);
			Room f = (Room)(l.get(i));
			f.setHunter(true);
			return true;
		}
		return false;
	}

	// If hunter is playing blindfolded
	private JCheckBox ctl_blindfolded = new JCheckBox("Blindfolded", false);
	public boolean isBlindfolded()  { return ctl_blindfolded.isSelected(); }

	// If hunter is about to shoot (pressed s)
	private boolean shooting;
	public boolean isShooting()  { return shooting; }

	// Controls for number of pits
	private JRadioButton ctl_zero = new JRadioButton("Zero pits", false);
	private JRadioButton ctl_one = new JRadioButton("One pit", false);
	private JRadioButton ctl_two = new JRadioButton("Two pits", true);
	private JRadioButton ctl_three = new JRadioButton("Three pits", false);

	// Controls for maze complexity
	private JRadioButton ctl_trivial = new JRadioButton("Trivial cave", false);
	private JRadioButton ctl_simple = new JRadioButton("Simple cave", true);
	private JRadioButton ctl_average = new JRadioButton("Average cave", false);
	private JRadioButton ctl_complex = new JRadioButton("Complex cave", false);

	// Button that starts game
	private JButton ctl_start;

	// Title of setup screen
	// Also displays outcome of previous game
	private JLabel ctl_title = new JLabel();

	// Setup panel visible prior to gameplay
	private JPanel ctl_setup;

	// Second graphics buffer
	private Image buffer;
	private Graphics graphics;

	// Create the title bar; called only from init
	private JPanel gui_title()  {
		JPanel p = new JPanel();
		message(MSG_DEFAULT);
		ctl_title.setFont(new Font("Monospaced", Font.BOLD, 24));
		p.add(ctl_title);
		return p;
	}

	// Create pit radio buttons; called only from gui_middle
	private JPanel gui_pitButtons()  {
		JPanel p = new JPanel(new GridLayout(4, 1));
		p.add(ctl_zero);
		p.add(ctl_one);
		p.add(ctl_two);
		p.add(ctl_three);
		ButtonGroup g = new ButtonGroup();
		g.add(ctl_zero);
		g.add(ctl_one);
		g.add(ctl_two);
		g.add(ctl_three);
		Font f = new Font("Monospaced", Font.PLAIN, 12);
		ctl_zero.setFont(f);
		ctl_one.setFont(f);
		ctl_two.setFont(f);
		ctl_three.setFont(f);
		p.setBorder(new EtchedBorder());
		return p;
	}

	// Create cave complexity radio buttons; called only from gui_middle
	private JPanel gui_caveButtons()  {
		JPanel p = new JPanel(new GridLayout(4, 1));
		p.add(ctl_trivial);
		p.add(ctl_simple);
		p.add(ctl_average);
		p.add(ctl_complex);
		ButtonGroup g = new ButtonGroup();
		g.add(ctl_trivial);
		g.add(ctl_simple);
		g.add(ctl_average);
		g.add(ctl_complex);
		Font f = new Font("Monospaced", Font.PLAIN, 12);
		ctl_trivial.setFont(f);
		ctl_simple.setFont(f);
		ctl_average.setFont(f);
		ctl_complex.setFont(f);
		p.setBorder(new EtchedBorder());
		return p;
	}

	// Create panel with image and two button groups
	// Called only from gui_main
	private JPanel gui_radios()  {
		JPanel p = new JPanel(new GridLayout(2, 1, 20, 20));
		p.add(gui_pitButtons());
		p.add(gui_caveButtons());
		return p;
	}

	// Create panel with start picture and blindfold control
	// Called only from gui_main
	private JPanel gui_pic()  {
		Image i = null;
		try  { i = getImage(new URL(getDocumentBase(), "big_wumpus.gif")); }
		catch(MalformedURLException e)  {  }
		ctl_start = new JButton("", new ImageIcon(i));
		ctl_start.addActionListener(this);
		ctl_start.setBorder(new EmptyBorder(0, 0, 0, 0));
		Font f = new Font("Monospaced", Font.PLAIN, 12);
		ctl_blindfolded.setFont(f);
		JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		p.add(ctl_blindfolded);
		JPanel q = new JPanel(new BorderLayout());
		q.add(ctl_start, BorderLayout.CENTER);
		q.add(p, BorderLayout.SOUTH);
		return q;
	}

	// Create (but do not assign) the setup panel
	// Called only from init, exactly once
	private JPanel gui_main()  {
		JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
		p.add(gui_radios());
		p.add(gui_pic());
		JPanel q = new JPanel(new BorderLayout(0, 0));
		q.add(gui_title(), BorderLayout.NORTH);
		q.add(p, BorderLayout.CENTER);
		return q;
	}

	public void init()  {
		setSize(WIDTH * Cell.WIDTH, HEIGHT * Cell.HEIGHT);
		buffer = createImage(WIDTH * Cell.WIDTH, HEIGHT * Cell.HEIGHT);
		graphics = buffer.getGraphics();
		Cell.init(this);
		addKeyListener(this);
		ctl_setup = gui_main();
		getContentPane().add(ctl_setup);
		ctl_start.requestFocus();
	}

	// Trap start action only; start gameplay
	public void actionPerformed(ActionEvent e)  {
		ctl_setup.setVisible(false);
		message(MSG_DEFAULT);
		requestFocus();
		shooting = false;
		int p = 0;
		if(ctl_one.isSelected()) p = 1;
		else if(ctl_two.isSelected()) p = 2;
		else if(ctl_three.isSelected()) p = 3;
		double t = 0;
		if(ctl_simple.isSelected()) t = 0.2;
		else if(ctl_average.isSelected()) t = 0.5;
		else if(ctl_complex.isSelected()) t = 0.7;
		while(!createMap(p, t));
		repaint();
	}

	// Control hunter during gameplay
	public void keyPressed(KeyEvent e)  {
		if(!ctl_setup.isVisible())  {
			int d = Cell.UNDEFINED;
			switch(e.getKeyCode())  {
				case KeyEvent.VK_KP_UP:
				case KeyEvent.VK_UP:
					if(hunter != null) d = Cell.NORTH;
					break;
				case KeyEvent.VK_KP_RIGHT:
				case KeyEvent.VK_RIGHT:
					if(hunter != null) d = Cell.EAST;
					break;
				case KeyEvent.VK_KP_DOWN:
				case KeyEvent.VK_DOWN:
					if(hunter != null) d = Cell.SOUTH;
					break;
				case KeyEvent.VK_KP_LEFT:
				case KeyEvent.VK_LEFT:
					if(hunter != null) d = Cell.WEST;
					break;
				case KeyEvent.VK_V:
					showMap();
					break;
				case KeyEvent.VK_S:
					if(hunter != null) shooting = !shooting;
					break;
				case KeyEvent.VK_R:
					shooting = false;
					ctl_setup.setVisible(true);
					break;
				default:
					return;
			}
			if(d != Cell.UNDEFINED) arrow(d);
			repaint();
		}
	}

	// Defined by contract with KeyListener
	// Not implemented here
	public void keyReleased(KeyEvent e)  {  }
	public void keyTyped(KeyEvent e)  {  }

	// Called when user presses arrow key, direction d
	// Either move hunter or shoot in d
	private void arrow(int d)  {
		Cell c = hunter.neighbor(d);
		if(c == null) return;
		if(shooting)  {
			shooting = false;
			c.visible = true;
			if(c.hasWumpus()) message(MSG_SHOT);
			else message(MSG_EATEN);
			hunter = null;
		}
		else  {
			hunter.setHunter(false);
			c.setHunter(true);
		}
	}

	// Called when user presses V button during game play
	// Reveal entire map and halt play
	private void showMap()  {
		for(int x = 0; x < WIDTH; x++)
			for(int y = 0; y < HEIGHT; y++)  {
				Cell c = cells[x][y];
				c.visible = true;
				if(c.isTunnel())  {
					Tunnel t = (Tunnel)(c);
					t.getPartner().visible = true;
				}
			}
		hunter = null;
	}

}