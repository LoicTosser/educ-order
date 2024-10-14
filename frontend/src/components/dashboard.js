import React, {Component} from "react";

class Activities extends Component {

    state = {
        activities: []
    };

    async componentDidMount() {
        const response = await fetch('/activities?start=2024-09-01T00:00:00Z&end=2024-10-01T00:00:00Z');
        const body = await response.json();
        this.setState({activities: body});
    }

    render() {
        const {activities} = this.state;
        return (
            <div className="App">
              <header className="App-header">
                <div className="App-intro">
                  <h2>Activitées</h2>
                  {activities.map(activity =>
                      <div key={activity.eventId}>
                        {activity.patient.firstName} {activity.beginDate} {activity.duration} {activity.activityType} {activity.location.address}
                      </div>
                  )}
                </div>
              </header>
            </div>
        );
    }
}

export default Activities