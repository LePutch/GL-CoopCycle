import dayjs from 'dayjs/esm';

import { CommandeStatus } from 'app/entities/enumerations/commande-status.model';

import { ICommande, NewCommande } from './commande.model';

export const sampleWithRequiredData: ICommande = {
  id: 5189,
  dateTime: dayjs('2023-03-19T01:10'),
  status: CommandeStatus['EN_COURS'],
};

export const sampleWithPartialData: ICommande = {
  id: 97722,
  dateTime: dayjs('2023-03-19T04:48'),
  status: CommandeStatus['LIVREE'],
};

export const sampleWithFullData: ICommande = {
  id: 4218,
  dateTime: dayjs('2023-03-19T10:11'),
  status: CommandeStatus['LIVREE'],
};

export const sampleWithNewData: NewCommande = {
  dateTime: dayjs('2023-03-18T20:34'),
  status: CommandeStatus['PRETE'],
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
