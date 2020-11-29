import React, { useRef, useEffect } from "react";

const Room = (props) => {
    const userVideo = useRef();
    const partnerVideo = useRef();
    const peerRef = useRef();
    const socketRef = useRef();
    const otherUser = useRef();
    const userStream = useRef();

    const sendMessage = (payload) => {
        socketRef.current.send(JSON.stringify(payload));
    }

    const callUser = (sessionId) => {
        peerRef.current = createPeer(sessionId);
        userStream.current.getTracks().forEach(track => peerRef.current.addTrack(track, userStream.current));
    }

    const createPeer = (sessionId) => {
        const peer = new RTCPeerConnection({
            iceServers: [
                {
                    urls: "stun:stun.stunprotocol.org"
                },
                {
                    urls: 'turn:numb.viagenie.ca',
                    credential: 'muazkh',
                    username: 'webrtc@live.com'
                },
            ]
        });

        peer.onicecandidate = handleICECandidateEvent;
        peer.ontrack = handleTrackEvent;
        peer.onnegotiationneeded = () => handleNegotiationNeededEvent(sessionId);

        return peer;
    }

    const handleNegotiationNeededEvent = (sessionId) => {
        peerRef.current.createOffer().then(offer => {
            return peerRef.current.setLocalDescription(offer);
        }).then(() => {
            const payload = {
                type: "offer",
                dest: sessionId,
                source: socketRef.current.id,
                roomID: props.match.params.roomID,
                data: {
                    sdp: peerRef.current.localDescription
                }
            };
            sendMessage(payload);
        }).catch(e => console.log(e));
    }

    const handleReceiveCall = (incoming) => {
        peerRef.current = createPeer();
        const desc = new RTCSessionDescription(incoming.data.sdp);
        peerRef.current.setRemoteDescription(desc).then(() => {
            userStream.current.getTracks().forEach(track => peerRef.current.addTrack(track, userStream.current));
        }).then(() => {
            return peerRef.current.createAnswer();
        }).then(answer => {
            return peerRef.current.setLocalDescription(answer);
        }).then(() => {
            const payload = {
                type: "answer",
                dest: incoming.source,
                source: socketRef.current.id,
                roomID: props.match.params.roomID,
                data: {
                    "sdp": peerRef.current.localDescription
                }
            }
            sendMessage(payload);
        })
    }

    const handleAnswer = (message) => {
        const desc = new RTCSessionDescription(message.data.sdp);
        peerRef.current.setRemoteDescription(desc).catch(e => console.log(e));
    }

    const handleICECandidateEvent = (e) => {
        if (e.candidate) {
            const payload = {
                type: "ice-candidate",
                dest: otherUser.current,
                source: socketRef.current.id,
                roomID: props.match.params.roomID,
                data: {
                    candidate: e.candidate,
                }
            }
            sendMessage(payload);
        }
    }

    const handleNewIceCandidate = (incoming) => {
        const candidate = new RTCIceCandidate(incoming);

            peerRef.current.addIceCandidate(candidate)
            .catch(e => console.log(e));
    }

    const handleTrackEvent = (e) => {
        console.log(e.streams);
        partnerVideo.current.srcObject = e.streams[0];
    };

    useEffect(() => {
        navigator.mediaDevices.getUserMedia = navigator.mediaDevices.getUserMedia || navigator.mediaDevices.webkitGetUserMedia || navigator.mediaDevices.mozGetUserMedia || navigator.mediaDevices.msGetUserMedia;

        navigator.mediaDevices.getUserMedia({ audio: true, video: true }).then(stream => {
            userVideo.current.srcObject = stream;
            userStream.current = stream;

            var uri = "ws://192.168.0.26:8080/signal";
            socketRef.current = new WebSocket(uri);
            socketRef.current.onopen = function(e) {
                console.log("open", e);
                socketRef.current.send(
                    JSON.stringify(
                        {
                            type: "join-room",
                            roomID: props.match.params.roomID
                        }
                    )
                )
            }

            socketRef.current.onclose = function(e) {
                console.log("close", e);
            }

            socketRef.current.onerror = function(e) {
                console.log("error", e);
            }

            socketRef.current.onmessage = function(e) {
                console.log("message", e);

                var message = JSON.parse(e.data);

                if (message.type === "other-user") {
                    callUser(message.dest);
                    otherUser.current = message.dest;
                }

                if (message.type === "user-joined") {
                    otherUser.current = message.dest;
                }

                if (message.type === "offer") {
                    handleReceiveCall(message);
                }

                if (message.type === "answer") {
                    handleAnswer(message);
                }

                if (message.type === "ice-candidate") {
                    handleNewIceCandidate(message.data.candidate);
                }
            }
        });

    }, []);


    return (
        <div>
            <video autoPlay ref={userVideo} />
            <video autoPlay ref={partnerVideo} />
        </div>
    );
};

export default Room;