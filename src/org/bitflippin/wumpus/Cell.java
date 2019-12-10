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

import java.awt.Graphics;
import java.awt.Image;
import java.net.MalformedURLException;
import java.net.URL;

abstract public class Cell  {

	// Directional constants
	public static final int NORTH = 0;
	public static final int EAST = 1;
	public static final int SOUTH = 2;
	public static final int WEST = 3;
	public static final int UNDEFINED = 4;

	// Image constants
	protected static final int ROOM = 0;
	protected static final int SWAMP = 1;
	protected static final int PIT = 2;
	protected static final int WUMPUS = 3;
	protected static final int NE_TUNNEL = 4;
	protected static final int SE_TUNNEL = 5;
	protected static final int NW_TUNNEL = 6;
	protected static final int SW_TUNNEL = 7;
	protected static final int NORMAL_HUNTER = 8;
	protected static final int SHOOTING_HUNTER = 9;
	protected static final int HIDDEN = 10;
	protected static final int TUNNELS = 11;
	protected static final int DOT = 12;

	// Dimensions of cells in pixels
	// Should match dimensions of GIF's
	public static final int WIDTH = 50;
	public static final int HEIGHT = 50;

	// List of images loaded by URL
	protected static Image pictures[] = new Image[13];

	// Determine child class
	abstract public boolean isRoom();
	abstract public boolean isTunnel();

	// Whether tunnel exits in each direction
	// Require d be directional constant from Cell
	abstract public boolean exits(int d);

	// Whether cell contains slime pit
	abstract public boolean hasPit();

	// Whether cell contains wumpus
	// Only one cell has this attribute
	abstract public boolean hasWumpus();

	// Whether cell contains swamp; mutually exclusive with pit
	abstract public boolean hasSwamp();

	// Whether cell contains lair; mutually exclusive with wumpus
	abstract public boolean hasLair();

	// Find "propagate neighbor" of c (nearest room to c)
	public Room propagate(Cell c)  {
		if(isRoom()) return (Room)(this);
		for(int d = 0; d < 4; d++)  {
			Cell n = neighbor(d);
			if(n != null && n != c) return n.propagate(this);
		}
		return null;
	}

	// Immediate neighboring cell
	// Require d be directional constant
	public Cell neighbor(int d)  {
		if(!exits(d)) return null;
		int ax = x;
		int ay = y;
		switch(d)  {
			case NORTH:
				ay = (y == 0) ? Wumpus.HEIGHT - 1 : y - 1;
				break;
			case EAST:
				ax = (x + 1) % Wumpus.WIDTH;
				break;
			case SOUTH:
				ay = (y + 1) % Wumpus.HEIGHT;
				break;
			case WEST:
				ax = (x == 0) ? Wumpus.WIDTH - 1 : x - 1;
				break;
		}
		Cell c = board.getCell(ax, ay);
		if(!c.exits((d + 2) % 4))  {
			Tunnel t = (Tunnel)(c);
			return t.getPartner();
		}
		return c;
	}

	// Whether cell has been uncovered yet
	// Used externally for showing map
	public boolean visible = false;

	// Whether cell contains hunter
	// Only one cell has this attribute
	protected boolean hunter = false;

	// Add or remove hunter from ourself
	// Inform board of change
	public void setHunter(boolean h)  {
		visible = true;
		if(hasWumpus())  {
			board.setHunter(null);
			board.message(Wumpus.MSG_EATEN);
		}
		else if(hasPit())  {
			board.setHunter(null);
			board.message(Wumpus.MSG_FALLEN);
		}
		else if(h)  {
			board.setHunter(this);
			hunter = true;
		}
		else  {
			if(board.isBlindfolded()) visible = false;
			hunter = false;
		}
	}

	// Coordinates of cell on board
	// Origin is (0,0)
	protected int x;
	protected int y;

	// Link to board
	// Common for all cells on map
	protected static Wumpus board;

	// Load pictures from URL
	// Associate cells with applet
	public static void init(Wumpus b)  {
		board = b;
		Graphics g = board.getGraphics();
		loadPicture(ROOM, "room.gif", g);
		loadPicture(SWAMP, "swamp.gif", g);
		loadPicture(PIT, "pit.gif", g);
		loadPicture(WUMPUS, "wumpus.gif", g);
		loadPicture(NE_TUNNEL, "ne_tunnel.gif", g);
		loadPicture(SE_TUNNEL, "se_tunnel.gif", g);
		loadPicture(NW_TUNNEL, "nw_tunnel.gif", g);
		loadPicture(SW_TUNNEL, "sw_tunnel.gif", g);
		loadPicture(NORMAL_HUNTER, "normal_hunter.gif", g);
		loadPicture(SHOOTING_HUNTER, "shooting_hunter.gif", g);
		loadPicture(HIDDEN, "hidden.gif", g);
		loadPicture(TUNNELS, "tunnels.gif", g);
		loadPicture(DOT, "dot.gif", g);
	}

	// Generate new cell, assign parameters
	// Used in map generation
	public Cell(int cx, int cy)  {
		x = cx;
		y = cy;
	}

	// Draw ourself onto g
	// Let g be graphics context of applet or paint parm
	abstract public void draw(Graphics g);

	// Load image file f into picture slot k
	// Draw it on g so it doesn't flicker
	// Used by init only
	private static void loadPicture(int k, String f, Graphics g)  {
		try  { pictures[k] = board.getImage(new URL(board.getDocumentBase(), f)); }
		catch(MalformedURLException e)  {  }
		g.drawImage(pictures[k], 0, 0, board);
	}

}