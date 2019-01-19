Run with command:

- Run "Mine Transition Systems" plugin in ProM to mine a transition system from a log file
	- Select backward, forward keys
	- Collection type = Set
	- Collection size limit = 1
	- Activity values: all
	- Transition values: all
	- Remove self-loop: no
	- Merge states with identical inflows: No
	- Merge stages with identical outflows: No

- Export the transition system to an .sg file (state transition system file) (Petrify plugin must be installed)

- Run Genet with the .sg file as an input, -rec and -pm parameters to mine Petrinet using divide and conquer approach

- The result would be an .g file (net file).

- Read the .g file into ProM as Petrinet using Petrify import plugin. The result is a Petrinet

- Measure fitness, precision, complexity