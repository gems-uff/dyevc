/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 * 
 */
package br.uff.ic.dyevc.gui;

import br.uff.ic.dyevc.tests.*;
import br.uff.ic.dyevc.model.topology.CloneRelationship;
import br.uff.ic.dyevc.model.topology.PullRelationship;
import br.uff.ic.dyevc.model.topology.PushRelationship;
import br.uff.ic.dyevc.model.topology.RepositoryInfo;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.AbstractEdgeShapeTransformer;
import edu.uci.ics.jung.visualization.decorators.ConstantDirectionalEdgeValueTransformer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.EdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.VertexLabelRenderer;

/**
 * Demonstrates jung support for drawing edge labels that
 * can be positioned at any point along the edge, and can
 * be rotated to be parallel with the edge.
 * 
 * @author Tom Nelson
 * 
 */
public class EdgeLabelDemoDyeVC2 extends JApplet {

    /**
	 * 
	 */
	private static final long serialVersionUID = -6077157664507049647L;

	/**
     * the graph
     */
    Graph<RepositoryInfo, CloneRelationship> graph;

    /**
     * the visual component and renderer for the graph
     */
    VisualizationViewer<RepositoryInfo, CloneRelationship> vv;
    
    /**
     */
    VertexLabelRenderer vertexLabelRenderer;
    EdgeLabelRenderer edgeLabelRenderer;
    
    ScalingControl scaler = new CrossoverScalingControl();
    
    /**
     * create an instance of a simple graph with controls to
     * demo the label positioning features
     * 
     */
    @SuppressWarnings("serial")
	public EdgeLabelDemoDyeVC2() {
        
        // create a simple graph for the demo
        graph = new SparseMultigraph<RepositoryInfo, CloneRelationship>();
        RepositoryInfo[] v = createVertices();
        createEdges(v);
        
        Layout<RepositoryInfo, CloneRelationship> layout = new CircleLayout<RepositoryInfo, CloneRelationship>(graph);
        vv =  new VisualizationViewer<RepositoryInfo, CloneRelationship>(layout, new Dimension(600,400));
        vv.setBackground(Color.white);

        vertexLabelRenderer = vv.getRenderContext().getVertexLabelRenderer();
        edgeLabelRenderer = vv.getRenderContext().getEdgeLabelRenderer();
        
        Transformer<CloneRelationship,String> stringer = new Transformer<CloneRelationship,String>(){
            public String transform(CloneRelationship e) {
                return e.toString();
            }
        };
        vv.getRenderContext().setEdgeLabelTransformer(stringer);
        vv.getRenderContext().setEdgeDrawPaintTransformer(new PickableEdgePaintTransformer<CloneRelationship>(vv.getPickedEdgeState(), Color.black, Color.cyan));
        vv.getRenderContext().setVertexFillPaintTransformer(new PickableVertexPaintTransformer<RepositoryInfo>(vv.getPickedVertexState(), Color.red, Color.yellow));
        // add my listener for ToolTips
        vv.setVertexToolTipTransformer(new ToStringLabeller<RepositoryInfo>());
        
        // create a frome to hold the graph
        final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
        Container content = getContentPane();
        content.add(panel);
        
        final DefaultModalGraphMouse<Integer,Number> graphMouse = new DefaultModalGraphMouse<Integer,Number>();
        vv.setGraphMouse(graphMouse);
        
        JButton plus = new JButton("+");
        plus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scaler.scale(vv, 1.1f, vv.getCenter());
            }
        });
        JButton minus = new JButton("-");
        minus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scaler.scale(vv, 1/1.1f, vv.getCenter());
            }
        });
        
        ButtonGroup radio = new ButtonGroup();
        JRadioButton lineButton = new JRadioButton("Line");
        lineButton.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line<RepositoryInfo,CloneRelationship>());
                    vv.repaint();
                }
            }
        });
        
        JRadioButton quadButton = new JRadioButton("QuadCurve");
        quadButton.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.QuadCurve<RepositoryInfo,CloneRelationship>());
                    vv.repaint();
                }
            }
        });
        
        JRadioButton cubicButton = new JRadioButton("CubicCurve");
        cubicButton.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.CubicCurve<RepositoryInfo,CloneRelationship>());
                    vv.repaint();
                }
            }
        });
        radio.add(lineButton);
        radio.add(quadButton);
        radio.add(cubicButton);

        graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        
        JCheckBox rotate = new JCheckBox("<html><center>EdgeType<p>Parallel</center></html>");
        rotate.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e) {
                AbstractButton b = (AbstractButton)e.getSource();
                edgeLabelRenderer.setRotateEdgeLabels(b.isSelected());
                vv.repaint();
            }
        });
        rotate.setSelected(true);
//        MutableDirectionalEdgeValue mv = new MutableDirectionalEdgeValue(.5, .7);
//        vv.getRenderContext().setEdgeLabelClosenessTransformer(mv);
//        JSlider directedSlider = new JSlider(mv.getDirectedModel()) {
//            public Dimension getPreferredSize() {
//                Dimension d = super.getPreferredSize();
//                d.width /= 2;
//                return d;
//            }
//        };
//        JSlider undirectedSlider = new JSlider(mv.getUndirectedModel()) {
//            public Dimension getPreferredSize() {
//                Dimension d = super.getPreferredSize();
//                d.width /= 2;
//                return d;
//            }
//        };
        
        JSlider edgeOffsetSlider = new JSlider(0,50) {
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.width /= 2;
                return d;
            }
        };
        edgeOffsetSlider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                JSlider s = (JSlider)e.getSource();
                AbstractEdgeShapeTransformer<RepositoryInfo,CloneRelationship> aesf = 
                    (AbstractEdgeShapeTransformer<RepositoryInfo,CloneRelationship>)vv.getRenderContext().getEdgeShapeTransformer();
                aesf.setControlOffsetIncrement(s.getValue());
                vv.repaint();
            }
        	
        });
        
        Box controls = Box.createHorizontalBox();

        JPanel zoomPanel = new JPanel(new GridLayout(0,1));
        zoomPanel.setBorder(BorderFactory.createTitledBorder("Scale"));
        zoomPanel.add(plus);
        zoomPanel.add(minus);

        JPanel edgePanel = new JPanel(new GridLayout(0,1));
        edgePanel.setBorder(BorderFactory.createTitledBorder("EdgeType Type"));
        edgePanel.add(lineButton);
        edgePanel.add(quadButton);
        edgePanel.add(cubicButton);

        JPanel rotatePanel = new JPanel();
        rotatePanel.setBorder(BorderFactory.createTitledBorder("Alignment"));
        rotatePanel.add(rotate);

        JPanel labelPanel = new JPanel(new BorderLayout());
        JPanel sliderPanel = new JPanel(new GridLayout(3,1));
        JPanel sliderLabelPanel = new JPanel(new GridLayout(3,1));
        JPanel offsetPanel = new JPanel(new BorderLayout());
        offsetPanel.setBorder(BorderFactory.createTitledBorder("Offset"));
//        sliderPanel.add(directedSlider);
//        sliderPanel.add(undirectedSlider);
        sliderPanel.add(edgeOffsetSlider);
        sliderLabelPanel.add(new JLabel("Directed", JLabel.RIGHT));
        sliderLabelPanel.add(new JLabel("Undirected", JLabel.RIGHT));
        sliderLabelPanel.add(new JLabel("Edges", JLabel.RIGHT));
        offsetPanel.add(sliderLabelPanel, BorderLayout.WEST);
        offsetPanel.add(sliderPanel);
        labelPanel.add(offsetPanel);
        labelPanel.add(rotatePanel, BorderLayout.WEST);
        
        JPanel modePanel = new JPanel(new GridLayout(2,1));
        modePanel.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
        modePanel.add(graphMouse.getModeComboBox());

        controls.add(zoomPanel);
        controls.add(edgePanel);
        controls.add(labelPanel);
        controls.add(modePanel);
        content.add(controls, BorderLayout.SOUTH);
        quadButton.setSelected(true);
    }
    
    /**
     * subclassed to hold two BoundedRangeModel instances that
     * are used by JSliders to move the edge label positions
     * @author Tom Nelson
     *
     *
     */
    class MutableDirectionalEdgeValue extends ConstantDirectionalEdgeValueTransformer<Integer,Number> {
        BoundedRangeModel undirectedModel = new DefaultBoundedRangeModel(5,0,0,10);
        BoundedRangeModel directedModel = new DefaultBoundedRangeModel(7,0,0,10);
        
        public MutableDirectionalEdgeValue(double undirected, double directed) {
            super(undirected, directed);
            undirectedModel.setValue((int)(undirected*10));
            directedModel.setValue((int)(directed*10));
            
            undirectedModel.addChangeListener(new ChangeListener(){
                public void stateChanged(ChangeEvent e) {
                    setUndirectedValue(new Double(undirectedModel.getValue()/10f));
                    vv.repaint();
                }
            });
            directedModel.addChangeListener(new ChangeListener(){
                public void stateChanged(ChangeEvent e) {
                    setDirectedValue(new Double(directedModel.getValue()/10f));
                    vv.repaint();
                }
            });
        }
        /**
         * @return Returns the directedModel.
         */
        public BoundedRangeModel getDirectedModel() {
            return directedModel;
        }

        /**
         * @return Returns the undirectedModel.
         */
        public BoundedRangeModel getUndirectedModel() {
            return undirectedModel;
        }
    }
    
    /**
     * create some vertices
     * @param count how many to create
     * @return the Vertices in an array
     */
    private RepositoryInfo[] createVertices() {
        RepositoryInfo[] v = new RepositoryInfo[5];
        
        RepositoryInfo dyevcssh = new RepositoryInfo();
        dyevcssh.setSystemName("dyevc");
        dyevcssh.setHostName("cmcdell");
        dyevcssh.setCloneName("dyevcssh");
        v[0] = dyevcssh;
        
        RepositoryInfo dyevc = new RepositoryInfo();
        dyevc.setSystemName("dyevc");
        dyevc.setHostName("cmcdell");
        dyevc.setCloneName("dyevc");
        v[1] = dyevc;
        
        RepositoryInfo dyevc3 = new RepositoryInfo();
        dyevc3.setSystemName("dyevc");
        dyevc3.setHostName("cmcdell");
        dyevc3.setCloneName("dyevc3");
        v[2] = dyevc3;
        
        RepositoryInfo dyevc2 = new RepositoryInfo();
        dyevc2.setSystemName("dyevc");
        dyevc2.setHostName("cmcdell");
        dyevc2.setCloneName("dyevc2");
        v[3] = dyevc2;
        
        RepositoryInfo gems = new RepositoryInfo();
        gems.setSystemName("dyevc");
        gems.setHostName("github");
        gems.setCloneName("gems/dyevc");
        v[4] = gems;
        
        return v;
    }

    /**
     * create edges for this demo graph
     * @param v an array of Vertices to connect
     */
    void createEdges(RepositoryInfo[] v) {
        
        CloneRelationship r1 = new PullRelationship(v[4], v[0]);
        CloneRelationship r2 = new PushRelationship(v[0], v[4]);
        CloneRelationship r3 = new PullRelationship(v[4], v[1]);
        CloneRelationship r4 = new PushRelationship(v[1], v[4]);
        CloneRelationship r5 = new PullRelationship(v[1], v[2]);
        CloneRelationship r6 = new PushRelationship(v[2], v[1]);
        CloneRelationship r7 = new PullRelationship(v[3], v[2]);
        CloneRelationship r8 = new PushRelationship(v[2], v[3]);
        CloneRelationship r9 = new PullRelationship(v[1], v[3]);
        CloneRelationship r10 = new PushRelationship(v[3], v[1]);

        graph.addEdge(r1, v[4], v[0], EdgeType.DIRECTED);
        graph.addEdge(r2, v[0], v[4], EdgeType.DIRECTED);
        graph.addEdge(r3, v[4], v[1], EdgeType.DIRECTED);
        graph.addEdge(r4, v[1], v[4], EdgeType.DIRECTED);
        graph.addEdge(r5, v[1], v[2], EdgeType.DIRECTED);
        graph.addEdge(r6, v[2], v[1], EdgeType.DIRECTED);
        graph.addEdge(r7, v[3], v[2], EdgeType.DIRECTED);
        graph.addEdge(r8, v[2], v[3], EdgeType.DIRECTED);
        graph.addEdge(r9, v[1], v[3], EdgeType.DIRECTED);
        graph.addEdge(r10, v[3], v[1], EdgeType.DIRECTED);
        
        for (RepositoryInfo repo: graph.getVertices()) {
            System.out.println(repo.getCloneName());
        }
        
        for (CloneRelationship r : graph.getEdges()) {
            System.out.println(r.getClass() + ": " + graph.getSource(r).getCloneName()
                    + " --> " + graph.getDest(r).getCloneName());
        }
    }

    /**
     * a driver for this demo
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container content = frame.getContentPane();
        content.add(new EdgeLabelDemoDyeVC2());
        frame.pack();
        frame.setVisible(true);
    }
}
