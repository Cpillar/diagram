package org.reactome.web.diagram.legends;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.InlineLabel;
import org.reactome.web.diagram.common.PwpButton;
import org.reactome.web.diagram.data.analysis.AnalysisType;
import org.reactome.web.diagram.data.analysis.ExpressionSummary;
import org.reactome.web.diagram.events.*;
import org.reactome.web.diagram.handlers.AnalysisResetHandler;
import org.reactome.web.diagram.handlers.AnalysisResultLoadedHandler;
import org.reactome.web.diagram.handlers.AnalysisResultRequestedHandler;
import org.reactome.web.diagram.handlers.DiagramRequestedHandler;
import org.reactome.web.diagram.util.slider.Slider;
import org.reactome.web.diagram.util.slider.SliderValueChangedEvent;
import org.reactome.web.diagram.util.slider.SliderValueChangedHandler;

import java.util.List;


/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class ExpressionControl extends LegendPanel implements ClickHandler, SliderValueChangedHandler,
        AnalysisResultRequestedHandler, AnalysisResultLoadedHandler, AnalysisResetHandler,
        DiagramRequestedHandler, ValueChangeHandler<Boolean> {

    private static Integer MIN_SPEED = 3000;
    private static Integer MAX_SPEED = 500;
    private Integer speed = MIN_SPEED / 2 + MAX_SPEED;

    private InlineLabel message;
    private ExpressionSummary expressionSummary;
    private int currentCol = 0;
    private Timer timer;

    private PwpButton rewindBtn;
    private PwpButton playBtn;
    private PwpButton pauseBtn;
    private PwpButton forwardBtn;
    private SpeedButton speedBtn;
    private PwpButton closeBtn;
    private Slider slider;

    public ExpressionControl(EventBus eventBus) {
        super(eventBus);

        //Setting the legend style
        LegendPanelCSS css = RESOURCES.getCSS();
        addStyleName(css.analysisControl());
        addStyleName(css.expressionControl());

        this.rewindBtn = new PwpButton("Show previous", css.rewind(), this);
        this.add(this.rewindBtn);

        this.playBtn = new PwpButton("Play", css.play(), this);
        this.add(this.playBtn);

        this.pauseBtn = new PwpButton("Pause", css.pause(), this);
        this.pauseBtn.setVisible(false);
        this.add(this.pauseBtn);

        this.forwardBtn = new PwpButton("Show next", css.forward(), this);
        this.add(this.forwardBtn);

        this.speedBtn = new SpeedButton(RESOURCES);
        this.speedBtn.setStyleName(css.speed());
        this.speedBtn.addValueChangeHandler(this);
        this.speedBtn.disable();
        this.add(this.speedBtn);

        this.slider = new Slider(100, 24, 0, 1, 0.5, false);
        this.slider.addSliderValueChangedHandler(this);
        this.slider.setVisible(false);
        this.slider.setStyleName(css.slide());
        this.add(this.slider);

        this.message = new InlineLabel();
        this.add(this.message);

        this.closeBtn = new PwpButton("Close", css.close(), this);
        this.add(this.closeBtn);

        this.initHandlers();
        this.initTimer();
        this.setVisible(false);
    }

    @Override
    public void onAnalysisResultLoaded(AnalysisResultLoadedEvent event) {
        if (!event.isReset() && event.getType().equals(AnalysisType.EXPRESSION)) {
            this.currentCol = 0;
            this.eventBus.fireEventFromSource(new ExpressionColumnChangedEvent(this.currentCol), this);
            this.expressionSummary = event.getExpressionSummary();
            this.setName();
            makeVisible(200); // Appear with delay
        } else {
            this.setVisible(false);
        }
    }

    @Override
    public void onAnalysisReset(AnalysisResetEvent event) {
        if (this.isVisible()) {
            if (this.timer.isRunning()) pause();
            this.message.setText("");
            this.expressionSummary = null;
            this.setVisible(false);
        }
    }

    @Override
    public void onClick(ClickEvent event) {
        Object source = event.getSource();

        if (source.equals(this.closeBtn))
            eventBus.fireEventFromSource(new AnalysisResetEvent(), this);
        else if (source.equals(this.rewindBtn))
            this.moveBackward();
        else if (source.equals(this.playBtn))
            this.play();
        else if (source.equals(this.pauseBtn))
            this.pause();
        else if (source.equals(this.forwardBtn))
            this.moveForward();
    }

    private void initHandlers() {
        this.eventBus.addHandler(AnalysisResultRequestedEvent.TYPE, this);
        this.eventBus.addHandler(AnalysisResultLoadedEvent.TYPE, this);
        this.eventBus.addHandler(AnalysisResetEvent.TYPE, this);
        this.eventBus.addHandler(DiagramRequestedEvent.TYPE, this);
    }

    @Override
    public void onValueChange(ValueChangeEvent<Boolean> event) {
        SpeedButton source = (SpeedButton) event.getSource();
        if (source.equals(this.speedBtn)) {
            this.slider.setVisible(source.isDown());
        }
    }

    @Override
    public void onSliderValueChanged(SliderValueChangedEvent event) {
        this.speed = (int) ((MAX_SPEED - MIN_SPEED) * event.getPercentage()) + MIN_SPEED;
        if (this.timer.isRunning()) {
            this.timer.cancel();
            this.timer.scheduleRepeating(this.speed);
        }
    }

    private void initTimer() {
        this.timer = new Timer() {
            @Override
            public void run() {
                moveForward();
            }
        };
    }

    private void moveBackward() {
        if (this.currentCol == 0) {
            this.currentCol = this.expressionSummary.getColumnNames().size() - 1;
        } else {
            this.currentCol -= 1;
        }
        this.setName();
        this.eventBus.fireEventFromSource(new ExpressionColumnChangedEvent(this.currentCol), this);
    }

    private void moveForward() {
        if (this.currentCol == this.expressionSummary.getColumnNames().size() - 1) {
            this.currentCol = 0;
        } else {
            this.currentCol += 1;
        }
        this.setName();
        this.eventBus.fireEventFromSource(new ExpressionColumnChangedEvent(this.currentCol), this);
    }

    private void pause() {
        this.rewindBtn.setEnabled(true);
        this.forwardBtn.setEnabled(true);
        this.speedBtn.setDown(false);
        this.speedBtn.disable();
        this.slider.setVisible(false);

        this.pauseBtn.setVisible(false);
        this.playBtn.setVisible(true);

        if (this.timer.isRunning()) {
            this.timer.cancel();
        }
    }

    private void play() {
        this.rewindBtn.setEnabled(false);
        this.forwardBtn.setEnabled(false);
        this.speedBtn.enable();

        this.playBtn.setVisible(false);
        this.pauseBtn.setVisible(true);

        this.moveForward();
        this.timer.scheduleRepeating(this.speed);
    }

    private void setName() {
        List<String> cols = this.expressionSummary.getColumnNames();
        String pos = (this.currentCol + 1) + "/" + cols.size();
        String name = cols.get(this.currentCol);
        this.message.setText(pos + " :: " + name);
    }

    @Override
    public void onAnalysisResultRequested(AnalysisResultRequestedEvent event) {
        this.setVisible(false);
    }

    @Override
    public void onDiagramRequested(DiagramRequestedEvent event) {
        this.setVisible(false);
    }


}
