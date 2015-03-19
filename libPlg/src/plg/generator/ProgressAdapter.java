package plg.generator;

/**
 * Empty adapter for a progress visualizer. It does nothing.
 * 
 * @author Andrea Burattin
 */
public class ProgressAdapter implements IProgressVisualizer {

	@Override
	public void setMinimum(int minimum) { }

	@Override
	public void setMaximum(int maximum) { }

	@Override
	public void inc() { }
}