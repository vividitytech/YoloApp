package x.leon;

public class Result {

	private float left;
	private float top;
	private float width;
	private float height;
	private float confidence;
	private String label;

	public Result(float left, float top, float width, float height, float confidence, String label) {
		this.left = left;
		this.top = top;
		this.width = width;
		this.height = height;
		this.confidence = confidence;
		this.label = label;
	}

	public float getLeft() {
		return left;
	}

	public float getTop() {
		return top;
	}

	public float getWidth() {
		return width;
	}

	public float getHeight() {
		return height;
	}

	public float getConfidence() {
		return confidence;
	}

	public String getLabel() {
		return label;
	}

}
