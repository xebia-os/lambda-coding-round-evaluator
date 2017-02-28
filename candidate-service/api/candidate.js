'use strict';

const uuid = require('uuid');
const AWS = require('aws-sdk');
const R = require('ramda');

AWS.config.setPromisesDependency(require('bluebird'));

const dynamoDb = new AWS.DynamoDB.DocumentClient();

module.exports.submit = (event, context, callback) => {
    console.log("Receieved request submit candidate details. Event is", event);
    const requestBody = JSON.parse(event.body);
    const fullname = requestBody.fullname;
    const email = requestBody.email;
    const experience = requestBody.experience;
    const skills = requestBody.skills;
    const recruiterEmail = requestBody.recruiterEmail;

    if (typeof fullname !== 'string' || typeof email !== 'string' || typeof experience !== 'number' || typeof skills !== 'string' || typeof recruiterEmail !== 'string') {
        console.error('Validation Failed');
        callback(new Error('Couldn\'t submit candidate because of validation errors.'));
        return;
    }

    const candidate = candidateInfo(fullname, email, experience, skills, recruiterEmail);
    const candidateSubmissionFx = R.composeP(submitCandidateEmailP, submitCandidateP, checkCandidateExistsP);

    candidateSubmissionFx(candidate)
        .then(res => {
            console.log(`Successfully submitted ${fullname}(${email}) candidate to system`);
            callback(null, successResponseBuilder(
                JSON.stringify({
                    message: `Sucessfully submitted candidate with email ${email}`,
                    candidateId: res.id
                }))
            );
        })
        .catch(err => {
            console.error('Failed to submit candidate to system', err);
            callback(null, failureResponseBuilder(
                409,
                JSON.stringify({
                    message: `Unable to submit candidate with email ${email}`
                })
            ))
        });
};


module.exports.list = (event, context, callback) => {
    console.log("Receieved request to list all candidates. Event is", event);
    var params = {
        TableName: process.env.CANDIDATE_TABLE,
        ProjectionExpression: "id, fullname, email"
    };
    const onScan = (err, data) => {
        if (err) {
            console.log('Scan failed to load data. Error JSON:', JSON.stringify(err, null, 2));
            callback(err);
        } else {
            console.log("Scan succeeded.");
            return callback(null, successResponseBuilder(JSON.stringify({
                candidates: data.Items
            })
            ));
        }
    };
    dynamoDb.scan(params, onScan);
};

module.exports.get = (event, context, callback) => {
    const params = {
        TableName: process.env.CANDIDATE_TABLE,
        Key: {
            id: event.pathParameters.id,
        },
    };
    dynamoDb.get(params)
        .promise()
        .then(result => {
            callback(null, successResponseBuilder(JSON.stringify(result.Item)));
        })
        .catch(error => {
            console.error(error);
            callback(new Error('Couldn\'t fetch candidate.'));
            return;
        });
};

const checkCandidateExistsP = (candidate) => {
    console.log('Checking if candidate already exists...');
    const query = {
        TableName: process.env.CANDIDATE_EMAIL_TABLE,
        Key: {
            "email": candidate.email
        }
    };
    return dynamoDb.get(query)
        .promise()
        .then(res => {
            if (R.not(R.isEmpty(res))) {
                return Promise.reject(new Error('Candidate already exists with email ' + email));
            }
            return candidate;
        });
}

const submitCandidateP = candidate => {
    console.log('submitCandidateP() Submitting candidate to system');
    const candidateItem = {
        TableName: process.env.CANDIDATE_TABLE,
        Item: candidate,
    };
    return dynamoDb.put(candidateItem)
        .promise()
        .then(res => candidate);
};


const successResponseBuilder = (body) => {
    return {
        statusCode: 200,
        headers: {
            'Content-Type': 'application/json',
            'Access-Control-Allow-Origin': '*'
        },
        body: body
    };
};

const failureResponseBuilder = (statusCode, body) => {
    return {
        statusCode: statusCode,
        headers: {
            'Content-Type': 'application/json',
            'Access-Control-Allow-Origin': '*'
        },
        body: body
    };
};


const submitCandidateEmailP = candidate => {
    console.log('Submitting candidate email');
    const candidateEmailInfo = {
        TableName: process.env.CANDIDATE_EMAIL_TABLE,
        Item: {
            candidate_id: candidate.id,
            email: candidate.email
        },
    };
    return dynamoDb.put(candidateEmailInfo)
        .promise();
}

const candidateInfo = (fullname, email, experience, skills, recruiterEmail) => {
    const timestamp = new Date().getTime();
    return {
        id: uuid.v1(),
        fullname: fullname,
        email: email,
        experience: experience,
        skills: skills,
        recruiterEmail: recruiterEmail,
        evaluated: false,
        submittedAt: timestamp,
        updatedAt: timestamp,
    };
};

