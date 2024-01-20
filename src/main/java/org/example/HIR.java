package org.example;

import java.util.ArrayList;

class HIR {
    class HIRBlock {
        ArrayList<IRExpression> expressions = new ArrayList<>();

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (IRExpression expr : expressions) {
                sb.append(expr.toString()).append("\n");
            }
            return sb.toString();
        }
    }

    abstract class IRExpression {
        String type;
        ArrayList<Graph.Node> operands = new ArrayList<>();

        public IRExpression(String type, Graph.Node operand) {
            this.type = type;
            operands.add(operand);
        }

        @Override
        public abstract String toString();
    }

    class IRBinaryExpression extends IRExpression {
        public IRBinaryExpression(String type, Graph.Node operand1, Graph.Node operand2) {
            super(type, operand1);
            operands.add(operand2);
        }

        @Override
        public String toString() {
            return String.format("(%s %s %s)", type, operands.get(0).name, operands.get(1).name);
        }
    }

    class IRFunctionCall extends IRExpression {
        public IRFunctionCall(String type, Graph.Node function) {
            super(type, function);
        }

        @Override
        public String toString() {
            return String.format("(%s %s)", type, operands.get(0).name);
        }
    }

    // 其他HIR元素的定义...
}
