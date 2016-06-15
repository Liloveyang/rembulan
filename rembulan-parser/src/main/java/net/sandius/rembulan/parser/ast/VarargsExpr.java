package net.sandius.rembulan.parser.ast;

public class VarargsExpr extends Expr {

	public VarargsExpr(SourceInfo src) {
		super(src);
	}

	@Override
	public Expr accept(Transformer tf) {
		return tf.transform(this);
	}

}