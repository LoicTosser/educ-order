import React, {useEffect, useState} from "react";
import Container from "react-bootstrap/Container";
import 'bootstrap/dist/css/bootstrap.min.css';
import axios from "axios";
import Navbar from "./navbar";
import { Duration, DateTime } from 'luxon';

const Activities = () => {

    const firstDayOfMonth = () => {
        const date = new Date();
        return new Date(date.getFullYear(), date.getMonth(), 1);
    }

    const lastDayOfMonth = () => {
        const date = new Date();
        return new Date(date.getFullYear(), date.getMonth() + 1, 0);
    }

    const [startDate, setStartDate] = useState(firstDayOfMonth());
    const [endDate, setEndDate] = useState(lastDayOfMonth());
    const [backendResponse, setBackendResponse] = useState({ start: firstDayOfMonth().toISOString(), endDate: lastDayOfMonth().toISOString(), activitySummaries: {}});

    const fetchActivities = async (startUTC, endUTC) => {
        axios.get(`http://localhost:8080/activities/summary?start=${startUTC}&end=${endUTC}`, {withCredentials: true})
            .then(response => {

                setBackendResponse(response.data);
            })
    };

    useEffect(() => {
        const startUTC = convertToUTC(startDate);
        const endUTC = convertToUTC(endDate);
    }, []);

    const handleInputChange = (event) => {
        const { name, value } = event.target;
        if (name === 'startDate') setStartDate(value);
        if (name === 'endDate') setEndDate(value);
    };

    const convertToUTC = (datetime) => {
        const date = new Date(datetime);
        return date.toISOString();
    };

    const handleSubmit = async (event) => {
        event.preventDefault();
        const startUTC = convertToUTC(startDate);
        const endUTC = convertToUTC(endDate);
        const response = fetchActivities(startUTC, endUTC);
        if (response.status !== 200) {
            alert('Failed to fetch activities');
        }
    };

    const formatDuration = (isoDuration) => {
        const duration = Duration.fromISO(isoDuration, {locale: 'fr'});
        return duration.toFormat('hh:mm', {round: false});
    };

    const formatDate = (isoDate) => {
        const date = DateTime.fromISO(isoDate).setLocale('fr');
        return date.toLocaleString(DateTime.DATETIME_SHORT);
    };

    function ActivitySummaries() {
        return Object.entries(backendResponse.activitySummaries).map(([institution, institutionActivities]) => (
            <div className="accordion-item" key={institution}>
                <h2 className="accordion-header">
                    <button className="accordion-button" type="button" data-bs-toggle="collapse"
                            data-bs-target={"#" + institution + "collapse"} aria-expanded="true" aria-controls={institution + "collapse"}>
                        {institution} - {formatDuration(institutionActivities.durations)}
                    </button>
                </h2>
                <div id={institution+"collapse"} className="accordion-collapse collapse"
                     data-bs-parent="#activities-summaries">
                    <div className="accordion-body">
                        <div className="accordion" id="instiution-activities">
                            <InstitutionActivities institutionActivities={institutionActivities}/>
                        </div>
                    </div>
                </div>
            </div>
        ))
            ;
    }

    function InstitutionActivities(props) {
        const institutionActivities = props.institutionActivities;
        return Object.entries(institutionActivities.patientActivities).map(([patient, patientActivities]) => (
            <div className="accordion-item" key={patient}>
                <h2 className="accordion-header">
                    <button className="accordion-button" type="button" data-bs-toggle="collapse"
                            data-bs-target={"#" + patient + "collapse"} aria-expanded="true"
                            aria-controls={patient + "collapse"}>
                        {patient} - {formatDuration(patientActivities.durations)}
                    </button>
                </h2>
                <div id={patient + "collapse"} className="accordion-collapse collapse show"
                     data-bs-parent="#institution-activities">
                    <div className="accordion-body">
                        <div className="accordion" id="ipatient-activities">
                            <PatientActivities patientActivities={patientActivities}/>
                        </div>
                    </div>
                </div>
        </div>
    ))
    }

    function PatientActivities(props) {
        const patientActivities = props.patientActivities;
        return Object.entries(patientActivities.activitiesByType).map(([activityType, typeActivities]) => (
            <div className="accordion-item" key={activityType}>
                <h2 className="accordion-header">
                    <button className="accordion-button" type="button" data-bs-toggle="collapse"
                            data-bs-target={"#" + activityType + "collapse"} aria-expanded="true"
                            aria-controls={activityType + "collapse"}>
                        {activityType} - {formatDuration(typeActivities.durations)}
                    </button>
                </h2>
                <div id={activityType + "collapse"} className="accordion-collapse collapse show"
                     data-bs-parent="#patient-activities">
                    <div className="accordion-body">
                        <div className="accordion" id="type-activities">
                            <TypeActivities typeActivities={typeActivities}/>
                        </div>
                    </div>
                </div>
            </div>))
    }

    function TypeActivities(props) {
        const typeActivities = props.typeActivities;
        const activities = typeActivities.activities;

        return <div>
            <table class="table table-striped">
                <thead>
                <tr>
                    <th scope="col">Date</th>
                    <th scope="col">Durée</th>
                    <th scope="col">Lieux</th>
                </tr>
                </thead>
                <tbody>
                {activities.map(activity => (
                    <tr key={activity.id}>
                        <td>{formatDate(activity.beginDate)}</td>
                        <td>{formatDuration(activity.duration)}</td>
                        <td>{activity.location.address}</td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    }

    function NoActivities() {
        return <p>No activities yet.</p>;
    }

    function Greeting(props) {
        if (backendResponse.activitySummaries === null || Object.keys(backendResponse.activitySummaries).length === 0) {
            return <NoActivities/>;
        }
        return <ActivitySummaries/>;
    }

    return (
        <>
            <Container as="main" className="py-4 px-3 mx-auto">
                <Navbar/>
                <h1>Récaptitulatif des activitées</h1>
                <form class="row justify-content-start mb-3" onSubmit={handleSubmit}>
                    <div className="col-4">
                        <label for="startDateInput" class="form-label">Date de début:</label>
                        <input
                            type="date"
                            name="startDate"
                            class="form-control"
                            id="startDateInput"
                            value={startDate}
                            onChange={handleInputChange}
                            required
                        />
                    </div>
                    <div className="col-4">
                        <label for="endDateInput" class="form-label">Date de fin:</label>
                        <input
                            type="date"
                            name="endDate"
                            class="form-control"
                            id="endDateInput"
                            value={endDate}
                            onChange={handleInputChange}
                            required
                        />
                    </div>
                    <div className="col-4">
                        <button type="submit" class="btn btn-primary">Lister interventions</button>
                    </div>
                </form>
                <div className="row">
                    <div className="accordion" id="activities-summaries">
                        <Greeting/>
                    </div>
                </div>
            </Container>
        </>
);
}
export default Activities