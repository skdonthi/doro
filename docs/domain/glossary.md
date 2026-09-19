# Domain glossary — Direct Load (Direktverkehr)

German terms first; the domain speaks German.

| Begriff | English | Meaning |
|---|---|---|
| Transportauftrag | transport order | customer books: from, to, when, what |
| Sendung / Ladung | shipment / load | goods: pallets, Lademeter, kg |
| Lademeter (LDM) | loading metre | 1 m of trailer floor, full width. Standard trailer: 13.6 LDM |
| Europalette | EUR pallet | 1.2 × 0.8 m, 0.4 LDM each, standard trailer takes 33 |
| Teilladung / Komplettladung | LTL / FTL | shares the truck / owns the truck |
| Direktverkehr | direct transport | door to door, no hub handling |
| Tour | tour | one vehicle, one day, ordered stops |
| Fahrzeug | vehicle | 7.5 t, 12 t, 40 t Sattelzug. Capacity: LDM, kg, pallets |
| Frachtführer / Subunternehmer | carrier / subcontractor | runs the truck, often a third party |
| Disponent | dispatcher | human who builds tours; the copilot's user |
| Abholung / Zustellung | pickup / delivery | each with a Zeitfenster (time window) |
| Laufzeit | transit time | pickup to delivery |
| Auslastung | utilisation | used LDM / total LDM; the dispatcher's KPI |
| Leerkilometer | empty kilometres | truck runs empty; money burned |
| Ablieferbeleg (POD) | proof of delivery | signed, photographed; ends the order |
| Vorschlag | proposal | copilot's suggested assignment; needs human approval |

## Bounded contexts

| Context | Owns | Style | Service |
|---|---|---|---|
| Auftrag (Order) | Order lifecycle: placed, amended, assigned, cancelled | event-sourced | services/order (Java) |
| Disposition (Dispatch) | Tour, Vehicle, Proposal, assignment | state + outbox | services/dispatch (NestJS) |
| Ausführung (Tracking) | pickup, transit, delivery, POD | stretch | — |

## Invariants (become the first tests)

- Pickup window ends before delivery window ends.
- An order declares at least one of: loading metres, pallets, weight.
- A tour's assigned LDM, pallets and weight never exceed the vehicle's capacity.
- A proposal is approved or rejected at most once, and only while OPEN.
- Assignment is only possible for orders in state PLACED.
