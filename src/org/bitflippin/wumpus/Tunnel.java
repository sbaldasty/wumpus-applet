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

public class Tunnel extends Cell  {

	// Determine child class
	// Defined by contract with Cell
	public boolean isRoom()  { return false; }
	public boolean isTunnel()  { return true; }

	// Whether tunnel exits in each direction
	// Require d be directional constant from Cell
	// Defined by contract with Cell
	private boolean exits[] = new boolean[4];
	public boolean exits(int d)  { return exits[d]; }

	// Partner tunnel with exits in opposite directions
	private Tunnel partner;
	public Tunnel getPartner()  { return partner; }

	// Whether cell contains slime pit
	public boolean hasPit()  { return false; }

	// Whether cell contains wumpus
	// Only one cell has this attribute
	public boolean hasWumpus()  { return false; }

	// Whether cell contains swamp; mutually exclusive with pit
	// Defined by contract with Cell
	public boolean hasSwamp()  { return false; }

	// Whether cell contains lair; mutually exclusive with wumpus
	// Defined by contract with Cell
	public boolean hasLair()  { return false; }

	// Generate north-exiting tunnel
	// Automatically generates its south-exiting partner tunnel
	public Tunnel(int cx, int cy)  {
		super(cx, cy);
		exits[NORTH] = true;
		exits[SOUTH] = false;
		double choice = Math.random();
		exits[EAST] = (choice > 0.5);
		exits[WEST] = !exits[EAST];
		partner = new Tunnel(cx, cy, this, choice);
	}

	// Generate south-exiting tunnel
	// Used only when north-exiting tunnel is created
	private Tunnel(int cx, int cy, Tunnel p, double choice)  {
		super(cx, cy);
		partner = p;
		exits[NORTH] = false;
		exits[SOUTH] = true;
		exits[EAST] = (choice < 0.5);
		exits[WEST] = !exits[EAST];
	}

	// Draw ourself onto g
	// Let g be graphics context of applet or paint parameter
	// Defined by contract with Cell
	public void draw(Graphics g)  {
		g.drawImage(pictures[HIDDEN], x * 50, y * 50, board);
		subdraw(g);
		partner.subdraw(g);
	}

	// Draw only this tunnel onto g
	// Let real draw handle clearing part and partner
	private void subdraw(Graphics g)  {
		int sx = x * 50;
		int sy = y * 50;
		if(!visible) return;
		int i = 0;
		if(exits[NORTH] && exits[EAST])
			i = NE_TUNNEL;
		else if(exits[SOUTH] && exits[EAST])
			i = SE_TUNNEL;
		else if(exits[NORTH] && exits[WEST])
			i = NW_TUNNEL;
		else
			i = SW_TUNNEL;
		g.drawImage(pictures[i], sx, sy, board);
		if(hunter)  {
			if(board.isShooting())
				i = SHOOTING_HUNTER;
			else
				i = NORMAL_HUNTER;
			if(exits(NORTH))
				sy += 4;
			else
				sy += 23;
			if(exits(WEST))
				sx += 4;
			else
				sx += 28;
			g.drawImage(pictures[i], sx, sy, board);
		}
	}

}