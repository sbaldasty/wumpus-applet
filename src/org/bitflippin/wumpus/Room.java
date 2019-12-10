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

public class Room extends Cell  {

	// Determine child class
	// Defined by contract with Cell
	public boolean isRoom()  { return true; }
	public boolean isTunnel()  { return false; }

	// Whether tunnel exits in each direction
	// Require d be directional constant from Cell
	// Defined by contract with Cell
	public boolean exits(int d)  { return true; }

	// Whether cell contains slime pit
	// Defined by contract with Cell
	private boolean pit = false;
	public boolean hasPit()  { return pit; }

	// Whether cell contains swamp; mutually exclusive with pit
	// Defined by contract with Cell
	private boolean swamp = false;
	public boolean hasSwamp()  { return swamp; }

	// Whether cell contains lair; mutually exclusive with wumpus
	// Defined by contract with Cell
	private boolean lair = false;
	public boolean hasLair()  { return lair; }

	// Whether cell contains wumpus
	// Only one cell has this attribute
	// Defined by contract with Cell
	private boolean wumpus = false;
	public boolean hasWumpus()  { return wumpus; }

	// Generate new cell, assign parameters
	// Carryover constructor of Cell
	public Room(int cx, int cy)  { super(cx, cy); }

	// Make this room into pit
	// Tell surrounding rooms to be swamps
	// Called in map construction
	public void addPit()  {
		pit = true;
		for(int i = 0; i < 4; i++)
			neighbor(i).propagate(this).swamp = true;
	}

	// Choose this room as home of wumpus
	// Tell two layers of neighbors to be lairs
	// Called in map construction
	public void addWumpus()  {
		wumpus = true;
		for(int i = 0; i < 4; i++)  {
			Room r1 = neighbor(i).propagate(this);
			if(r1 != this) r1.lair = true;
			for(int j = 0; j < 4; j++)  {
				Room r2 = r1.neighbor(j).propagate(r1);
				if(r2 != this) r2.lair = true;
			}
		}
	}

	// Draw ourself onto g
	// Let g be graphics context of applet or paint parameter
	// Defined by contract with Cell
	public void draw(Graphics g)  {
		int sx = x * WIDTH;
		int sy = y * HEIGHT;
		if(!visible)  {
			g.drawImage(pictures[HIDDEN], sx, sy, board);
			return;
		}
		int i = 0;
		if(pit)
			i = PIT;
		else if(swamp)
			i = SWAMP;
		else
			i = ROOM;
		g.drawImage(pictures[i], sx, sy, board);
		if(lair) g.drawImage(pictures[DOT], sx, sy, board);
		if(wumpus) g.drawImage(pictures[WUMPUS], sx, sy, board);
		if(hunter)  {
			if(board.isShooting())
				i = SHOOTING_HUNTER;
			else
				i = NORMAL_HUNTER;
			g.drawImage(pictures[i], sx + 16, sy + 14, board);
		}
	}

}